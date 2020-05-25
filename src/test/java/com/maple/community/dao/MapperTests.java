package com.maple.community.dao;


import com.maple.community.CommunityApplication;
import com.maple.community.dao.UserMapper;
import com.maple.community.entity.DiscussPost;
import com.maple.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussMapper discussMapper;

    @Test
    public void testSelectById(){
        User user = userMapper.selectById(102);
        System.out.println(user);
    }

    @Test
    public void testSelectPost(){
        List<DiscussPost> list = discussMapper.selectDiscussPost(0,1,10,0);
        for(DiscussPost post : list){
            System.out.println(post);
        }

        int rows = discussMapper.selectDiscussPostRows(0);
        System.out.println(rows);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUserName("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }


}
