package com.community.config.interceptor;

import com.community.bean.User;
import com.community.service.DateService;
import com.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户访问拦截，记录访问次数
 */
@Component
public class DateInterceptor implements HandlerInterceptor {

    @Autowired
    DateService dateService;
    @Autowired
    HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //统计UV
        String ip = request.getRemoteHost();
        dateService.recordUV(ip);

        //统计DAU
        User user = hostHolder.getUser();
        if (user != null){
            dateService.recordDAU(user.getId());
        }

        return true;
    }
}
