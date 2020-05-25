package com.maple.community.controller;

import com.maple.community.entity.Event;
import com.maple.community.event.EventProducer;
import com.maple.community.service.LikeService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.CommunityUtil;
import com.maple.community.util.HostHolder;
import com.maple.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {

    @Autowired
    private LikeService likeService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId){
        int userId = hostHolder.getUser().getId();
        likeService.like(userId, entityType, entityId, entityUserId);
        // 数量
        long count = likeService.findEntityLikeCount(entityType, entityId);
        // 状态
        int status = likeService.findEntityLikeStatus(userId, entityType,
                entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("likeCount",count);
        map.put("likeStatus", status);

        // 触发点赞事件
        if (status == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(userId)
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }
        if(entityType == ENTITY_TYPE_POST){
            // 计算帖子分数
            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }
        return CommunityUtil.getJSONString(0,null, map);
    }


}
