package com.maple.community.event;

import com.alibaba.fastjson.JSONObject;
import com.maple.community.entity.DiscussPost;
import com.maple.community.entity.Event;
import com.maple.community.entity.Message;
import com.maple.community.service.DiscussPostService;
import com.maple.community.service.ElasticsearchService;
import com.maple.community.service.MessageService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * 事件消费者
 */
@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String command;

    @Value("${wk.image.storage}")
    private String imageStorage;

    @Value("${qiniu.key.access}")
    private String access;

    @Value("${qiniu.key.secret}")
    private String secret;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_LIKE,TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("消息的内容为空");
            return;
        }
        // 将 event json 转为 对象
        Event event = JSONObject.parseObject(record.value().toString(),
                Event.class);
        if (event == null){
            logger.error("消息格式错误");
            return;
        }

        // 发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        // 设置内容
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        if (!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("此消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null){
            logger.error("消息格式错误");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("此消息为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),
                Event.class);
        if (event == null){
            logger.error("消息格式错误");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    // 消费分享事件
    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record){
        if (record == null || record.value() == null){
            logger.error("此消息的内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),
                Event.class);
        if (event == null){
            logger.error("格式转换错误");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = command + " --quality 75 " + htmlUrl+ " "+
                imageStorage+"/"+fileName+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.error("生成长图成功");
        }catch (IOException e){
            logger.error("生成长图失败");
            e.printStackTrace();
        }
        // 启用定时器，监视图片是否上传
        UploadTask task = new UploadTask(fileName,suffix);
        Future future = threadPoolTaskScheduler.scheduleAtFixedRate(task,500);
        task.setFuture(future);
    }

    class UploadTask implements Runnable{
        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值，停止定时器
        private Future future;
        // 上传时间
        private long startTime;
        // 上传次数
        private int uploadTimes;
        public UploadTask(String fileName, String suffix){
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // 生成图片失败
            if(System.currentTimeMillis() - startTime > 30000){
                logger.error("执行时间过长，中止任务"+fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3){
                logger.error("上传次数过多，中止任务"+fileName);
                future.cancel(true);
                return;
            }
            // 执行任务
            String path = imageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists()){
                logger.info(String.format("开始第%d次上传【%s】",++uploadTimes,fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                // 生成上传凭证
                Auth auth = Auth.create(access,secret);
                String uploadToken = auth.uploadToken(shareBucketName,null,3600,policy);
                // 指定上传机房
                UploadManager uploadManager = new UploadManager(new Configuration(Region.region1()));
                try {
                    Response response = uploadManager.put(path, null,uploadToken);
                    // 处理响应结果
                    JSONObject jsonObject = JSONObject.parseObject(response.bodyString());
                    if (jsonObject == null || jsonObject.get("code") == null ||!jsonObject.get("code").equals(0)){
                        logger.error(String.format("第%d次上传失败【%s】",uploadTimes,fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功【%s】",uploadTimes,fileName));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.error(String.format("第%d次上传失败【%s】",uploadTimes,fileName));
                }
            }else {
                logger.info("等待图片生成【"+fileName+"】");
            }

        }
    }

}

