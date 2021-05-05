package com.community.config.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * mq的配置
 */
@Configuration
public class MqConfig {
    //交换机名称
    public static final String ITEM_TOPIC_EXCHANGE = "community";
    //队列名称
    public static final String QUEUE_COMMENT = "comment";
    public static final String QUEUE_LIKE = "like";
    public static final String QUEUE_FOLLOW= "follow";

    //首先配置一个交换机，没有这个交换机就会自动创建
    @Bean
    public Exchange exchange(){
        return ExchangeBuilder.topicExchange(ITEM_TOPIC_EXCHANGE).durable(true).build();
    }

    //配置一个队列，没有这个队列会创建这个队列
    @Bean
    public Queue comment(){
        return QueueBuilder.durable(QUEUE_COMMENT).build();
    }
    @Bean
    public Queue like(){
        return QueueBuilder.durable(QUEUE_LIKE).build();
    }
    @Bean
    public Queue follow(){
        return QueueBuilder.durable(QUEUE_FOLLOW).build();
    }

    //配置我们的Binding，也就是绑定关系，被封装为一个类,吧我们配置的数据传入
    @Bean
    public Binding bindingComment(Exchange exchange, Queue comment){
        return BindingBuilder.bind(comment).to(exchange).with(QUEUE_COMMENT).noargs();
    }
    @Bean
    public Binding bindingLike(Exchange exchange, Queue like){
        return BindingBuilder.bind(like).to(exchange).with(QUEUE_LIKE).noargs();
    }
    @Bean
    public Binding bindingFollow(Exchange exchange, Queue follow){
        return BindingBuilder.bind(follow).to(exchange).with(QUEUE_FOLLOW).noargs();
    }
}
