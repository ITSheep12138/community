package com.community.config.interceptor;

import com.community.bean.LoginTicket;
import com.community.bean.User;
import com.community.service.UserService;
import com.community.utils.CookieUtil;
import com.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 自定义拦截器，用于获取登录的信息
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Autowired
    private HostHolder hostHolder;

    /**
     * 请求处理前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //通过判断是否有这个对应的cookie来判断是否登录
        String ticket = CookieUtil.getTicket(request, "ticket");

        if (ticket != null){
            //根据这个ticket查询对应的凭证信息
            LoginTicket loginTicket = userService.getLoginTicket(ticket);
            if (loginTicket != null && loginTicket.getStatus() == 0 && loginTicket.getExpired().after(new Date())){
                //然后获取这个登录的信息
                int userId = loginTicket.getUserId();
                User user = userService.findUserById(userId);
                //存入HostHolder
                hostHolder.setUser(user);
            }
        }
        return true;
    }


    /**
     * 将数据存入ModelAndView，方便提取数据
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        //通过判断是否有这个对应的cookie来判断是否登录
        String ticket = CookieUtil.getTicket(request, "ticket");
        LoginTicket loginTicket = userService.getLoginTicket(ticket);
        if (user != null && modelAndView != null && loginTicket.getStatus() != 1){
            modelAndView.addObject("loginUser",user);
        }
    }

    /**
     *
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.removeUser();
    }
}
