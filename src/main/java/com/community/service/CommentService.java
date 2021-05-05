package com.community.service;

import com.community.bean.Comment;
import com.community.dao.CommentMapper;
import com.community.utils.CommunityConstant;
import com.community.utils.HostHolder;
import com.community.utils.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;
import org.unbescape.html.HtmlEscape;

import java.util.List;

@Component
public class CommentService {

    @Autowired(required = false)
    CommentMapper commentMapper;
    @Autowired
    DiscussPostService postService;
    @Autowired
    SensitiveFilter sensitiveFilter;

    /**
     * 分页查询评论
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> selectCommentsPage(int entityType, int entityId, int offset, int limit){
        return commentMapper.selectCommentsPage(entityType,entityId,offset,limit);
    }

    /**
     * 评论的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public int selectCount(int entityType,int entityId){
        return commentMapper.selectCount(entityType,entityId);
    }

    /**
     * 新增帖子,增加事务，隔离级别为：读已提交，传播路径为：REQUIRED
     * @param comment
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public void addComment(Comment comment){
        if (comment == null){
            throw new IllegalArgumentException("新增帖子参数不可以为空!");
        }
        //首先是过滤敏感词和标签
        String filterText = sensitiveFilter.filterText(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(filterText);

        //然后就是插入这个帖子
        commentMapper.insertComment(comment);
        //更新帖子
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            int count = commentMapper.selectCount(comment.getEntityType(), comment.getEntityId());
            postService.updateCommentCount(comment.getEntityId(),count);
        }
    }

    /**
     * 根据评论id查询
     * @param id
     * @return
     */
    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
