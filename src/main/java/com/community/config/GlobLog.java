package com.community.config;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 使用AOP配置日志
 */
@Component
@Aspect
public class GlobLog {

    private Logger logger = LoggerFactory.getLogger(GlobLog.class);

    /**
     * service包下记录日志
     */
    @Pointcut("execution(* com.community.service.*.*(..))*")
    public void pointCut(){}

    @After("pointCut()")
    public void afterService(JoinPoint joinPoint){
        //日志记录格式：用户[IP]在xxx[时间]访问了xxx[方法]
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null || requestAttributes.getRequest() == null){
            return;
        }
        String host = requestAttributes.getRequest().getRemoteHost();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd").format(new Date());
        String pointMethod = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        logger.info(String.format("用户: [%s]在 %s 访问了：%s",host,date,pointMethod));
    }
}
