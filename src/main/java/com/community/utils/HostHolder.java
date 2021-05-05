package com.community.utils;

import com.community.bean.User;
import org.springframework.stereotype.Component;

/**
 * 用于代替Session，防止并发情况下出现数据错误
 */
@Component
public class HostHolder {
    //ThreadLocal相当于线程自己的变量
    private ThreadLocal<User> threadLocal = new InheritableThreadLocal<User>();

    public void setUser(User user){
        threadLocal.set(user);
    }

    public User getUser(){
        return threadLocal.get();
    }

    public void removeUser(){
        threadLocal.remove();
    }

}
