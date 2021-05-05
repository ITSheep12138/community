package com.comunity;

import com.community.CommunityApplication;
import com.community.bean.DiscussPost;
import com.community.bean.Message;
import com.community.dao.DiscussPostMapper;
import com.community.dao.MessageMapper;
import com.community.dao.UserMapper;
import com.community.service.DiscussPostService;
import com.community.utils.MailClient;
import com.community.utils.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = CommunityApplication.class)
class CommunityApplicationTests {
    @Autowired
    DiscussPostMapper postMapper;
    @Autowired
    DiscussPostService postService;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    UserMapper userMapper;
    @Autowired
    MailClient mailClient;
    @Autowired
    TemplateEngine templateEngine;

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    SensitiveFilter sensitiveFilter;

    @Test
    void testRabbitTemplate() {
        Map map = new HashMap();
        map.put("msg","妹妹");
        //这个是发送的消息的一个API第一个参数为exchange的名字，第二个为消息队列的名字，最后为我们的数据
        //rabbitTemplate.convertAndSend("test","test.haha",map);

        //这个取出一个消息队列中的信息的API，直接指定这个名字即可
        Object convert = rabbitTemplate.receiveAndConvert("haha");
        System.out.println(convert);
    }

    @Test
    void contextLoads() {
        List<DiscussPost> discussPosts = postService.findDiscussPosts(0, 0, 10,0);
        for (DiscussPost discussPost : discussPosts) {
            System.out.println(discussPost);
        }
        System.out.println(postMapper.selectDiscussPostRows(0));
    }

    /**
     * 测试邮件服务
     */
    @Test
    void testMail() {
        //thymeleaf模板填充
        Context context = new Context();
        context.setVariable("name","张三");
        String process = templateEngine.process("/mail/demo", context);
        System.out.println(process);
        //mailClient.sendMail("15387036082@163.com","测试",process);
    }

    @Test
    void testSensitive() {
        /*for (Message message : messageMapper.getMessageList(111, 0, 20)) {
            System.out.println(message);
        }
        System.out.println("==============================");
        System.out.println(messageMapper.getMessageCount(111));
        System.out.println("==============================");
        System.out.println(messageMapper.getLetters("111_112",0,20));
        System.out.println("==============================");
        System.out.println(messageMapper.getLetterCount("111_112"));*/
        System.out.println(messageMapper.getLetterCountUnRead(111,null));
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testRedisTemplate(){
        redisTemplate.opsForValue().set("test",1);
    }

}

