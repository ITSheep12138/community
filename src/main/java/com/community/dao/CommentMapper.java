package com.community.dao;

import com.community.bean.Comment;

import java.util.List;

/**
 * 帖子评论以及回复
 */
public interface CommentMapper {

    /**
     * 分页查询评论
     * @param entityType 是什么类型的评论
     * @param entityId 某个类型的回复
     * @param offset 起始页
     * @param limit 每页条数
     * @return
     */
    List<Comment> selectCommentsPage(int entityType,int entityId,int offset,int limit);

    /**
     * 评论数量
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCount(int entityType,int entityId);

    /**
     * 插入评论
     * @param comment
     * @return
     */
    void insertComment(Comment comment);

    Comment selectCommentById(int id);
}
