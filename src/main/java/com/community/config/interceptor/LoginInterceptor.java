package com.community.config.interceptor;

import com.community.config.myannotation.LoginCheck;
import com.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * 自定义拦截器，判断是否登录，防止未登录通过URL访问到后台信息
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;

    /**
     * 拦截访问需要登陆的请求
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //然后根据这个方法是否标注了这个注解来判断是否登录
            Method method = handlerMethod.getMethod();
            LoginCheck loginCheck = method.getAnnotation(LoginCheck.class);

            //只要标注了这个注解的方法就需要登录才可访问
            if (loginCheck != null && hostHolder.getUser() == null){
                //重定向到登录页面
                response.sendRedirect(request.getContextPath()+"/login");
                return false;
            }
        }
        return true;
    }
}
