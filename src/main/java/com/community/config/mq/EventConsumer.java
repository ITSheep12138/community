package com.community.config.mq;

import com.alibaba.fastjson.JSONObject;
import com.community.bean.Event;
import com.community.service.MessageService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer {

    private Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    /**
     * 监听这几个队列
     * new String(message.getBody()) 将数据转换成字符串
     * @param message
     */
    @RabbitListener(queues = {MqConfig.QUEUE_COMMENT,MqConfig.QUEUE_FOLLOW,MqConfig.QUEUE_LIKE})
    public void handleMessage(Message message){
        if (message == null || message.getBody() == null){
            logger.error("消息内容为空！");
            return;
        }
        Event event = JSONObject.parseObject(new String(message.getBody()), Event.class);
        logger.info(event.toString());
        if (event == null){
            logger.error("消息格式错误！");
            return;
        }
        //封装数据
        com.community.bean.Message myMessage = new com.community.bean.Message();
        myMessage.setFromId(CommunityConstant.SYSTEM_USER);
        myMessage.setToId(event.getEntityUserId());
        myMessage.setCreateTime(new Date());
        myMessage.setConversationId(event.getTopic());

        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());

        //把额外的信息都存入message中
        if (!event.getData().isEmpty()){
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(),entry.getValue());
            }
        }
        myMessage.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(myMessage);
    }
}
