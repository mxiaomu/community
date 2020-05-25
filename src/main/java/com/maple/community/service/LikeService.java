package com.maple.community.service;

import com.maple.community.util.CommunityConstant;
import com.maple.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

/**
 * 点赞业务
 */

@Service
public class LikeService implements CommunityConstant {

    @Autowired
    private RedisTemplate redisTemplate;


    // 点赞
    // 增加对用户的赞
    public void like(int userId, int entityType, int entityId, int entityUserId){

        redisTemplate.execute(new SessionCallback(){
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType,entityId);
                String userLikeKey = RedisUtil.getUserLikeKey(entityUserId);
                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
                redisOperations.multi();
                if (isMember){
                    redisTemplate.opsForSet().remove(entityLikeKey,userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                }else{
                    redisTemplate.opsForSet().add(entityLikeKey,userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return redisOperations.exec();
            }
        });
    }

    // 查询某个实体的点赞数量
    public long findEntityLikeCount(int entityType, int entityId){
        String redisKey = RedisUtil.getEntityLikeKey(entityType,entityId);
        BoundSetOperations<String,Object> operations = redisTemplate.boundSetOps(redisKey);
        return operations.size();
    }
    // 查询某个用户对实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String redisKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(redisKey, userId) ? 1 : 0;
    }
    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
