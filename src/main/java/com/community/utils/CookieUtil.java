package com.community.utils;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * 用于操作Cookie的工具
 */
public class CookieUtil {

    /**
     * 判断是否有对应的cookie，有则返回这个cookie的值
     * @param request
     * @param name
     * @return
     */
    public static String getTicket(HttpServletRequest request,String name){
        if (request == null || name == null){
            throw new IllegalArgumentException("CookieUtil.getTicket 参数不能为空！");
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length != 0){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)){
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

}
