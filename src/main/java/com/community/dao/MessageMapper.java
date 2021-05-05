package com.community.dao;

import com.community.bean.Message;

import java.util.List;

/**
 * 私信消息DAO
 */
public interface MessageMapper {

    /**
     * 查询当前用户首页展示的消息列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> getMessageList(int userId,int offset,int limit);

    /**
     * 查询当前用户一共有多少个会话
     * @param userId
     * @return
     */
    int getMessageCount(int userId);

    /**
     * 分页查询当前会话的消息列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> getLetters(String conversationId,int offset,int limit);

    /**
     * 查询某个会话的私信数量
     * @param conversationId
     * @return
     */
    int getLetterCount(String conversationId);

    /**
     * 查询未读的数量
     * @param userId
     * @param conversationId 用户判定是会话还是总数量
     * @return
     */
    int getLetterCountUnRead(int userId,String conversationId);

    /**
     * 发送消息
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 更新未读的消息
     * @param ids
     * @return
     */
    int updateMessage(List<Integer> ids,int status);

    /**
     * 查询某个主题下的最新通知
     * @param userId
     * @param topic
     * @return
     */
    Message getLatestNotice(int userId,String topic);

    /**
     * 查询某个主题的通知数量
     * @param userId
     * @param topic
     * @return
     */
    int getNoticeCount(int userId,String topic);

    /**
     * 查询某个主题的未读通知数量
     * @param userId
     * @param topic
     * @return
     */
    int getUnReadNoticeCount(int userId,String topic);

    /**
     * 查询某个主题的未读通知数量
     * @param userId
     * @param topic
     * @return
     */
    List<Message> getNoticeDetailPage(int userId,String topic,int offset,int limit);
}
