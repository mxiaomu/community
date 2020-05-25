package com.maple.community.controller;

import com.google.code.kaptcha.Producer;
import com.maple.community.entity.User;
import com.maple.community.service.UserService;
import com.maple.community.util.CommunityConstant;
import com.maple.community.config.KaptchaConfig;
import com.maple.community.util.CommunityUtil;
import com.maple.community.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {

    private final static Logger logger = LoggerFactory.getLogger(LoginController.class);

    // 项目路径
    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private KaptchaConfig kaptchaConfig;

    @Autowired
    private RedisTemplate redisTemplate;


    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){

        return "/site/login";
    }
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(Model model){

        return "/site/register";
    }

    @RequestMapping(path = "register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,String> map = userService.register(user);
        if (map == null || map.isEmpty()){

            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了一封激活邮件，" +
                    "请尽快激活");
            model.addAttribute("target","/index");

            return "/site/operate-result";
        }

        model.addAttribute("usernameMsg",map.get("usernameMsg"));
        model.addAttribute("passwordMsg",map.get("passwordMsg"));
        model.addAttribute("emailMsg",map.get("emailMsg"));

        return "/site/register";
    }


    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId,
                             @PathVariable("code") String code){
        int result = userService.activation(userId, code);

        if (result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","注册成功, 您的账号已经可以正常使用了");
            model.addAttribute("target","/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg","您的账号已经激活成功，请不要重复操作");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，请提供正确的激活码");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }

    // 获取验证码
    @RequestMapping(path = "kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        Producer producer = kaptchaConfig.kaptchaProducer();
        String text = producer.createText();
        BufferedImage image = producer.createImage(text);
        // 将验证码存入 Session
        // session.setAttribute("kaptcha",text);

        // 生成Cookie
        String kapthchaOwner = CommunityUtil.generateUUID();
        Cookie cookie = new Cookie("kaptchaOwner", kapthchaOwner);
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie);
        // 存入Redis中
        String kaptchaKey = RedisUtil.getKaptchaKey(kapthchaOwner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS); // 设置过期时间

        response.setContentType("image/png");
        // 将图片输出到浏览器
        try {
            OutputStream stream = response.getOutputStream();
            ImageIO.write(image,"png", stream);
        } catch (IOException e) {
            logger.error("生成验证码失败:"+e.getMessage());
        }
    }

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model,@CookieValue("kaptchaOwner") String kaptchaOwner,/* HttpSession session,*/ HttpServletResponse response){
        // 检查验证码
        // String kaptcha = (String) session.getAttribute("kaptcha");
        String kaptcha = null;
        if (StringUtils.isNotBlank(kaptchaOwner)){
            String kaptchaKey = RedisUtil.getKaptchaKey(kaptchaOwner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if (StringUtils.isBlank(code) || StringUtils.isBlank(kaptcha) || !StringUtils.equalsAnyIgnoreCase(code, kaptcha)){
            model.addAttribute("codeMsg","验证码错误");
            return "site/login";
        }
        // 检查账号密码
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;

        Map<String,String> map = userService.login(username, password, expiredSeconds);

        if (map.containsKey("ticket")){
            Cookie cookie = new Cookie("ticket", map.get("ticket"));
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSeconds);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        // 默认为Get请求
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }


}
