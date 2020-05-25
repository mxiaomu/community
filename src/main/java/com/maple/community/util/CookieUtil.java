package com.maple.community.util;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;


public class CookieUtil {
    public static String getValue(HttpServletRequest request, String name){
        if (request == null)
            throw new IllegalArgumentException("请求对象不能为空");
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for(Cookie cookie : cookies){
            if (cookie.getName().equals(name))
                return cookie.getValue();
        }
        return null;
    }
}
