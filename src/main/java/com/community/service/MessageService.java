package com.community.service;

import com.community.bean.Message;
import com.community.dao.MessageMapper;
import com.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Component
public class MessageService {

    @Autowired(required = false)
    private MessageMapper messageMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    /**
     * 查询当前用户首页展示的消息列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> getMessageList(int userId, int offset, int limit){
        return messageMapper.getMessageList(userId,offset,limit);
    }

    /**
     * 查询当前用户一共有多少个会话
     * @param userId
     * @return
     */
    public int getMessageCount(int userId){
        return messageMapper.getMessageCount(userId);
    }

    /**
     * 分页查询当前会话的消息列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    public List<Message> getLetters(String conversationId, int offset, int limit){
        return messageMapper.getLetters(conversationId,offset,limit);
    }

    /**
     * 查询某个会话的私信数量
     * @param conversationId
     * @return
     */
    public int getLetterCount(String conversationId){
        return messageMapper.getLetterCount(conversationId);
    }

    /**
     * 查询未读的数量
     * @param userId
     * @param conversationId 用户判定是会话还是总数量
     * @return
     */
    public int getLetterCountUnRead(int userId,String conversationId){
        return messageMapper.getLetterCountUnRead(userId,conversationId);
    }

    /**
     * 发送消息，过滤敏感词
     * @param message
     * @return
     */
    public int addMessage(Message message){
        //过滤敏感词
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(sensitiveFilter.filterText(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    /**
     * 更新为已读状态
     * @param ids
     * @return
     */
    public int updateMessage(List<Integer> ids){
        return messageMapper.updateMessage(ids,1);
    }


    /**
     * 查询某个主题下的最新通知
     * @param userId
     * @param topic
     * @return
     */
    public List<Message> getNoticeDetailPage(int userId,String topic,int offset,int limit){
        return messageMapper.getNoticeDetailPage(userId,topic,offset,limit);
    }

    /**
     * 查询某个主题下的最新通知
     * @param userId
     * @param topic
     * @return
     */
    public Message getLatestNotice(int userId,String topic){
        return messageMapper.getLatestNotice(userId,topic);
    }

    /**
     * 查询某个主题的通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int getNoticeCount(int userId,String topic){
        return messageMapper.getNoticeCount(userId,topic);
    }


    /**
     * 查询某个主题的未读通知数量
     * @param userId
     * @param topic
     * @return
     */
    public int getUnReadNoticeCount(int userId,String topic){
        return messageMapper.getUnReadNoticeCount(userId,topic);
    }
}
