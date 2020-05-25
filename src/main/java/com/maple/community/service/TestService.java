package com.maple.community.service;

import com.maple.community.dao.DiscussMapper;
import com.maple.community.dao.UserMapper;
import com.maple.community.entity.DiscussPost;
import com.maple.community.entity.User;
import com.maple.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;


@Service
public class TestService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussMapper discussMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public String save1(){
        // 新建用户
        User user = new User();
        user.setUserName("coder");
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
        user.setHeaderUrl("http://image.nowcoder.com/header/99t.png");
        user.setEmail("1433187543@qq.com");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 新建帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("Hello");
        post.setContent("过年好！");
        post.setCreateTime(new Date());
        discussMapper.insertDiscussPost(post);

        Integer.valueOf("abc");

        return "ok";
    }


    public String save2(){
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        return transactionTemplate.execute(new TransactionCallback<String>() {

            @Override
            public String doInTransaction(TransactionStatus transactionStatus) {
                // 新建用户
                User user = new User();
                user.setUserName("coder");
                user.setSalt(CommunityUtil.generateUUID().substring(0,5));
                user.setPassword(CommunityUtil.md5("123"+user.getSalt()));
                user.setHeaderUrl("http://image.nowcoder.com/header/99t.png");
                user.setEmail("1433187543@qq.com");
                user.setCreateTime(new Date());
                userMapper.insertUser(user);

                // 新建帖子
                DiscussPost post = new DiscussPost();
                post.setUserId(user.getId());
                post.setTitle("Hello");
                post.setContent("过年好！");
                post.setCreateTime(new Date());
                discussMapper.insertDiscussPost(post);

                Integer.valueOf("abc");
                return "ok";
            }
        });

    }
}
