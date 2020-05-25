package com.maple.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testString(){
        String key = "test:id";
        redisTemplate.opsForValue().set(key,1);
        System.out.println(redisTemplate.opsForValue().get(key));
        redisTemplate.opsForValue().increment(key);
        System.out.println(redisTemplate.opsForValue().get(key));
    }

    @Test
    public void testHash(){
        String key = "test:user";
        String hashKey = "username";
        String hashValue = "mufeng";
        redisTemplate.opsForHash().put(key,hashKey,hashValue);
        System.out.println(redisTemplate.opsForHash().get(key,hashKey));

    }

    @Test
    public void testList(){
        String key = "test:list";
        String[] values = {"110","119","120"};
        for(String value : values){
            redisTemplate.opsForList().rightPush(key,value);
        };
        System.out.println(redisTemplate.opsForList().index(key,0));
    }

    @Test
    public void testSets(){
        String key = "test:set";
        redisTemplate.opsForSet().add(key,"刘备","关羽","张飞","赵云","黄忠","马超");
        Set<Object> sets = redisTemplate.opsForSet().members(key);
        assert sets != null;
        for (Object set : sets) {
            System.out.println(set.toString());
        }
    }

    @Test
    public void testZSets(){
        String key = "test:zset";
        redisTemplate.opsForZSet().add(key, "刘备", 10);
        redisTemplate.opsForZSet().add(key, "关羽", 9);
        Set<Object> set = redisTemplate.opsForZSet().range(key,0,1);
        for(Object o : set){
            System.out.println(o.toString());
        }
    }

    @Test
    public void testKeys(){
        String key = "test:student";
        Set<String> set = redisTemplate.keys("*");
        assert set != null;
        for(String value : set){
            System.out.println(value);
        }
        System.out.println(redisTemplate.hasKey(key));
    }

    @Test
    public void testTransactional(){
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                // 开启事务
                redisOperations.multi();
                String key = "test:set";
                redisOperations.opsForSet().add(key,"曹操");
                redisOperations.opsForSet().add(key,"典韦");

                System.out.println(redisOperations.opsForSet().members(key));
                return redisOperations.exec();
            }
        });
    }

    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";
        for(int i = 0 ; i < 100000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 0 ; i < 100000; i++){
            int r = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }
        long result = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(result);
    }

    @Test
    public void testHyperLogLogUnion(){
        String redisKey2 = "test:hll:02";
        for(int i = 0 ; i <= 10000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for(int i = 5001 ; i <= 15000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for(int i = 10001 ; i <= 20000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }
        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);
        long result = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(result);
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";
        redisTemplate.opsForValue().setBit(redisKey,0,true);
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,2,true);

        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,5));

        Object result = redisTemplate.execute((RedisCallback<Object>) connection -> connection.bitCount(redisKey.getBytes()));

        System.out.println(result);
    }
}
