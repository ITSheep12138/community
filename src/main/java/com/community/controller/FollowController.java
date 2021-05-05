package com.community.controller;

import com.community.bean.Event;
import com.community.bean.Page;
import com.community.bean.User;
import com.community.config.mq.EventProducer;
import com.community.config.mq.MqConfig;
import com.community.config.myannotation.LoginCheck;
import com.community.service.FollowService;
import com.community.service.UserService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 * 关注接口
 */
@Controller
public class FollowController {

    @Autowired
    private FollowService followService;
    @Autowired
    private UserService userService;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private HostHolder hostHolder;


    /**
     * 关注方法
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginCheck
    @PostMapping("/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(1,"你还没有登录");
        }
        followService.follow(user.getId(),entityType,entityId);

        Event event = new Event();
        event.setTopic(MqConfig.QUEUE_FOLLOW)
                .setUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId);
        eventProducer.fireEvent(event);


        return CommunityUtil.getJSONString(0,"关注成功");
    }

    /**
     * 取消关注方法
     * @param entityType
     * @param entityId
     * @return
     */
    @LoginCheck
    @PostMapping("/unFollow")
    @ResponseBody
    public String unFollow(int entityType,int entityId){
        User user = hostHolder.getUser();
        followService.unFollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"取关成功！");
    }

    /**
     * 关注列表
     * @return
     */
    @GetMapping("/followee/{userId}")
    public String getFollowees(@PathVariable int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if (user == null) throw new RuntimeException("该用户不存在");

        model.addAttribute("user",user);
        page.setLimit(5);
        page.setRows((int) followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER));
        page.setPath("/followee/"+userId);

        List<Map<String, Object>> userList = followService.getFollowees(userId, page.getOffset(), page.getLimit());
        for (Map<String, Object> map : userList) {
            User u = (User) map.get("user");
            map.put("hasFollowed",hasFollow(u.getId()));
        }

        model.addAttribute("userList",userList);

        return "site/followee";
    }


    /**
     * 粉丝列表
     * @return
     */
    @GetMapping("/follower/{userId}")
    public String getFollowers(@PathVariable int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if (user == null) throw new RuntimeException("该用户不存在");

        model.addAttribute("user",user);
        page.setLimit(5);
        page.setRows((int) followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER,userId));
        page.setPath("/follower/"+userId);

        List<Map<String, Object>> userList = followService.getFollowers(userId, page.getOffset(), page.getLimit());
        for (Map<String, Object> map : userList) {
            User u = (User) map.get("user");
            map.put("hasFollowed",hasFollow(u.getId()));
        }

        model.addAttribute("userList",userList);

        return "site/follower";
    }


    /**
     * 判断某个用户是否关注了这个人
     * @param userId
     * @return
     */
    private boolean hasFollow(int userId){
        if (hostHolder.getUser() == null) return false;
        return followService.hasFollow(hostHolder.getUser().getId(),CommunityConstant.ENTITY_TYPE_USER,userId);
    }
}
