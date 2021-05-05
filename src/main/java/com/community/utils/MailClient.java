package com.community.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;

/**
 * 邮箱发送组件
 */
@Component
public class MailClient {

    Logger logger = LoggerFactory.getLogger(MailClient.class);

    //spring的发送组件
    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送邮件方法
     * @param to 收件人
     * @param subject 主题
     * @param context 内容
     */
    public void sendMail(String to,String subject,String context){
        try {
            //需要发送的Mime类型数据，我们自己的数据需要填充到这个里面
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            //构建数据
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(context,true);   //支持HTML格式数据
            javaMailSender.send(message);
        } catch (Exception e) {
            logger.error("邮件发送失败："+e.getMessage());
        }
    }


}
