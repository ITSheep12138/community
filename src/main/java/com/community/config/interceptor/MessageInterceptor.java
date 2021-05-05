package com.community.config.interceptor;

import com.community.bean.User;
import com.community.service.MessageService;
import com.community.utils.HostHolder;
import org.omg.PortableInterceptor.Interceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;
    @Autowired
    MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null){
            int unReadNoticeCount = messageService.getUnReadNoticeCount(user.getId(), null);
            int letterCountUnRead = messageService.getLetterCountUnRead(user.getId(), null);
            modelAndView.addObject("addUnReadCount",unReadNoticeCount+letterCountUnRead);
        }
    }
}
