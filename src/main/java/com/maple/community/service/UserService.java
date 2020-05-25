package com.maple.community.service;

import com.maple.community.dao.LoginTicketMapper;
import com.maple.community.dao.UserMapper;
import com.maple.community.entity.LoginTicket;
import com.maple.community.entity.User;
import com.maple.community.util.CommunityConstant;
import com.maple.community.util.CommunityUtil;
import com.maple.community.util.MailClient;
import com.maple.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

   /* @Autowired
    private LoginTicketMapper loginTicketMapper;*/

   @Autowired
   private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int userId){
        User user = getCache(userId);
        if (user == null){
            user = initCache(userId);
        }
        return user;
    }

    public Map<String,String> register(User user){
        Map<String,String> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(user.getUserName())){
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }

        User u = userMapper.selectByName(user.getUserName());
        if (u != null){
            map.put("usernameMsg", "用户名已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if (u!=null){
            map.put("emailMsg","邮箱已被注册");
            return map;
        }
        // 注册
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getSalt() + user.getPassword()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",
                new Random().nextInt(1000)));
        user.setCreateTime(new Date());

        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    // 修改密码
    public Map<String, String> updatePassword(User user, String password){
        Map<String,String> map = new HashMap<>();
        if (user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (user.getPassword().equals(password)){
            map.put("passworld","新密码与原密码不能相同");
            return map;
        }
        String newPassword = CommunityUtil.md5(user.getSalt()+password);
        userMapper.updatePassword(user.getId(),newPassword);
        return map;
    }


    /**
     * 验证激活状态
     */
    public int activation(int userId, String activationCode){
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return ACTIVATION_REPEAT;
        }else if (user.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String,String> login(String username, String password, int expiredSeconds){
        Map<String,String> map = new HashMap<>();

        if (StringUtils.isBlank(username)){
            map.put("usernameMsg","该账号不能为空");
            return map;
        }

        if (StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        // 验证账号
        User user = userMapper.selectByName(username);
        if (user == null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        // 验证状态
        if (user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }

        password = CommunityUtil.md5(user.getSalt()+password);
        if (!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误");
            return map;
        }
        // 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        // 生成登陆凭证
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpires(new Date(System.currentTimeMillis()+expiredSeconds*1000));
        // loginTicketMapper.insertLoginTicket(loginTicket);
        String redisKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());

        return map;
    }



    // 退出
    public void logout(String ticket){
        // loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey, loginTicket);

    }

    // 查询登陆凭证
    public LoginTicket findLoginTicket(String ticket){
        // return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    // 更新用户头像
    public Integer updateHeaderUrl(Integer userId, String headerUrl){
        int rows = userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    public User findUserByName(String userName){
        return userMapper.selectByName(userName);
    }

    // 优先从缓存中取数据
    public User getCache(int userId){
        String userKey = RedisUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    // 取不到时初始化缓存数据
    public User initCache(int userId){
        String userKey = RedisUtil.getUserKey(userId);
        User user = userMapper.selectById(userId);
        redisTemplate.opsForValue().set(userKey,user,3600 , TimeUnit.SECONDS);
        return user;
    }

    // 数据变更时删除缓存信息
    public void clearCache(int userId){
        String redisKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getGrantedAuthorities(int userId){
        User user = findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add((GrantedAuthority) () -> {
            switch (user.getType()){
                case 1:
                    return AUTHORITY_ADMIN;
                case 2:
                    return AUTHORITY_MODERATOR;
                default:
                    return AUTHORITY_USER;
            }
        });
        return list;
    }
}
