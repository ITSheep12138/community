package com.community.config;

import com.community.config.interceptor.DateInterceptor;
import com.community.config.interceptor.LoginInterceptor;
import com.community.config.interceptor.LoginTicketInterceptor;
import com.community.config.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 拓展springMVC的功能
 */
@Component
@EnableCaching
public class WebMvcConfig implements WebMvcConfigurer {

    private final static  String[] excludePaths = {"/css/**","/img/**","/js/**"};
    @Autowired
    LoginTicketInterceptor loginTicketInterceptors;
    @Autowired
    LoginInterceptor loginInterceptor;
    @Autowired
    MessageInterceptor messageInterceptor;
    @Autowired
    DateInterceptor dateInterceptor;
    /**
     * 添加一个拦截器，处理登录之前是否已登录。
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //注意这里的顺序，必须这样写，因为每个Interceptor被注入都是按照书写的代码顺序进行的，
        registry.addInterceptor(loginTicketInterceptors).excludePathPatterns(excludePaths);
        registry.addInterceptor(loginInterceptor).excludePathPatterns(excludePaths);
        registry.addInterceptor(messageInterceptor).excludePathPatterns(excludePaths);
        registry.addInterceptor(dateInterceptor).excludePathPatterns(excludePaths);
    }


}
