package com.maple.community.quartz;

import com.maple.community.entity.DiscussPost;
import com.maple.community.service.DiscussPostService;
import com.maple.community.service.ElasticsearchService;
import com.maple.community.service.LikeService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.RedisUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job,CommunityConstant{

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    // 纪元
    private static final Date epoch ;
    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化纪元失败", e);
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);
        if (operations.size() <= 0){
            logger.info("【任务取消】，没有需要刷新的帖子");
            return;
        }
        logger.info("【任务开始】正在刷新帖子分数"+operations.size());
        while (operations.size() > 0){
            this.refresh((Integer) operations.pop());
        }
        logger.info("【任务结束】,帖子分数刷新完毕");
    }

    private void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        if (post == null){
            logger.error("该帖子不存在");
            return;
        }
        // 是否为精华帖
        boolean wonderful = post.getStatus() == 1;
        // 评论数量
        int commentCount = post.getCommentCount();
        // 点赞数量
        int likeCount = (int) likeService.findEntityLikeCount(CommunityConstant.ENTITY_TYPE_POST,
                postId);
        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // 分数
        double score =  Math.log10(Math.max(w,1)) + (post.getCreateTime().getTime()-epoch.getTime())/(1000 * 3600 * 24);
        // 更新帖子分数
        discussPostService.updateScore(postId,score);
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
