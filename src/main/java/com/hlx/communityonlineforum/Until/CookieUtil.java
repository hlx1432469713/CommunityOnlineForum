package com.hlx.communityonlineforum.Until;

import lombok.val;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 获取Request请求中指定 name 的 cookie
 */
public class CookieUtil {

    public static String getValue(HttpServletRequest httpServletRequest,String name){
        if (httpServletRequest == null && name == null)
            throw new IllegalStateException("参数为空！");
         Cookie[] cookies = httpServletRequest.getCookies();
         if (cookies != null){
             for (Cookie cookie : cookies) {
                 if(cookie.getName().equals(name))
                     return cookie.getValue();
             }
         }
         return null;
    }
}
