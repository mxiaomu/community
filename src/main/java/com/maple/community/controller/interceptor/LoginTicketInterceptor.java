package com.maple.community.controller.interceptor;

import com.maple.community.entity.LoginTicket;
import com.maple.community.entity.User;
import com.maple.community.service.UserService;
import com.maple.community.util.CookieUtil;
import com.maple.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {


    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    // 在Handler之前执行
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        String ticket = CookieUtil.getValue(request, "ticket");
        if (ticket != null){
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            if (loginTicket != null &&
                loginTicket.getStatus() == 0 &&
                loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.setUser(user);
                // 构建用户的认证结果，并存入SecurityContext中
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        user,user.getPassword(),userService.getGrantedAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
            }
        }
        return true;
    }

    // 在Controller之后，TemplateEngine之前
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            modelAndView.addObject("loginTicket", user);
        }
    }
    // TemplateEngine之后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
        SecurityContextHolder.clearContext();
    }
}