package com.maple.community.controller;

import com.maple.community.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;
import java.io.ObjectInputStream;
import java.net.http.HttpResponse;
import java.util.Collection;

@Controller
@RequestMapping("/test")
public class TestDemo {


    // 测试Cookie

    @RequestMapping(path = "/setCookie", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        // 设置Cookie的有效路径
        cookie.setPath("/maple/test/");
        // 设置Cookie的生命周期
        cookie.setMaxAge(60 * 10);
        response.addCookie(cookie);
        return "set cookie";
    }

//    @RequestMapping(path = "/getCookie", method = RequestMethod.GET)
//    @ResponseBody
//    public String getCookie(HttpServletRequest request,HttpServletResponse response){
//        Cookie[] cookies = request.getCookies();
//        StringBuilder buffer = new StringBuilder();
//        for(Cookie cookie : cookies){
//            buffer.append(cookie.getValue());
//        }
//        return buffer.toString();
//    }

    @RequestMapping(path = "/getCookie", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code, HttpServletResponse response){
        System.out.println(code);
        return "get Cookie";
    }

    @RequestMapping(path = "/setSession", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","mufeng");
        return "setSession";
    }

    @RequestMapping(path = "/getSession", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "getSession";
    }

    @PostMapping("/ajax")
    @ResponseBody
    public String testAJAX(String name, int age){
        System.out.println(name+":"+age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }



}
