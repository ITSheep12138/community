package com.community.config.myannotation;

import java.lang.annotation.*;

/**
 * 这个注解用来标识需要登录才可访问的方法
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginCheck {
}
