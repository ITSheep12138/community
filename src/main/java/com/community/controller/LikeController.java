package com.community.controller;

import com.community.bean.Event;
import com.community.bean.User;
import com.community.config.mq.EventProducer;
import com.community.config.mq.MqConfig;
import com.community.config.myannotation.LoginCheck;
import com.community.service.LikeService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 点赞的接口
 */
@Controller
public class LikeController {

    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param entityType
     * @param entityId
     * @return 返回点赞数量和该用户的点赞状态
     */
    @LoginCheck
    @PostMapping("/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        User user = hostHolder.getUser();

        //调用点赞的service
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //查询点赞数量和点赞的状态
        int status = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        long likeCount = likeService.getLikeCount(entityType, entityId);
        Map<String,Object> map = new HashMap<>();
        map.put("status",status);
        map.put("likeCount",likeCount);

        //点赞消息通知
        if (status == 1){
            Event event = new Event();
            event.setTopic(MqConfig.QUEUE_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId);
            eventProducer.fireEvent(event);
        }

        if (entityType == CommunityConstant.ENTITY_TYPE_POST){
            //计算帖子的分数
            String postScoreKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(postScoreKey,postId);
        }

        return CommunityUtil.getJSONString(0,null,map);
    }

}
