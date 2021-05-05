package com.community.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.community.bean.Event;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息的生产者
 */
@Component
public class EventProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param event
     */
    public void fireEvent(Event event){
        rabbitTemplate.convertAndSend(MqConfig.ITEM_TOPIC_EXCHANGE,
                event.getTopic(),
                JSONObject.toJSONString(event));
    }

}
