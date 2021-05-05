package com.community.controller;

import com.community.bean.Comment;
import com.community.bean.DiscussPost;
import com.community.bean.Event;
import com.community.config.mq.EventProducer;
import com.community.config.mq.MqConfig;
import com.community.config.myannotation.LoginCheck;
import com.community.service.CommentService;
import com.community.service.DiscussPostService;
import com.community.utils.CommunityConstant;
import com.community.utils.HostHolder;
import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    CommentService commentService;
    @Autowired
    HostHolder hostHolder;
    @Autowired
    EventProducer eventProducer;
    @Autowired
    DiscussPostService postService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 帖子新增的接口，特别注意的是需要传入discussPostId，因为如果评论的是评论，重定向的帖子id数据就无法从comment中取出来
     * @param discussPostId
     * @param comment
     * @return
     */
    @LoginCheck
    @PostMapping("/add/{discussPostId}")
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment){

        //补充数据
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        comment.setCreateTime(new Date());
        commentService.addComment(comment);

        //发送评论消息
        Event event = new Event();
        event.setTopic(MqConfig.QUEUE_COMMENT)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType())
                .setData("postId",discussPostId);
        if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_POST){
            DiscussPost target = postService.selectDiscussPostById(comment.getEntityId());
            event.setEntityId(target.getUserId());
            //计算帖子的分数
            String postScoreKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,discussPostId);

        } else if (comment.getEntityType() == CommunityConstant.ENTITY_TYPE_REPLAY){
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        eventProducer.fireEvent(event);
        return "redirect:/discussPost/detail/"+discussPostId;
    }

}
