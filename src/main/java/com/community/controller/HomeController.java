package com.community.controller;

import com.community.bean.DiscussPost;
import com.community.bean.Page;
import com.community.bean.User;
import com.community.service.DiscussPostService;
import com.community.service.LikeService;
import com.community.service.MessageService;
import com.community.service.UserService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    DiscussPostService postService;
    @Autowired
    UserService userService;
    @Autowired
    MessageService messageService;
    @Autowired
    LikeService likeService;
    @Autowired
    HostHolder hostHolder;

    /**
     * 查询首页
     * Page 这个对象会被SpringMVC自动注入request中，所以不需要我们手动的注入这个数据
     */
    @GetMapping("/index")
    public String getIndex(Model model,Page page,
                           @RequestParam(name = "orderMode",defaultValue = "0") int orderMode){
        //封装这个page
        page.setRows(postService.findDiscussPostRows(0));   //封装总条数
        page.setPath("/index?orderMode="+orderMode);

        //首先查询所有的帖子
        List<DiscussPost> list = postService.findDiscussPosts(0, page.getOffset(), page.getLimit(),orderMode);
        //然后帖子的作者也需要查询出来
        List<Map<String,Object>> discussPosts = new ArrayList<Map<String,Object>>();
        for (DiscussPost discussPost : list) {
            //根据每个帖子的用户id关联查询用户
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user",user);    //帖子的作者
            map.put("post",discussPost);
            long likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
            map.put("likeCount",likeCount);
            discussPosts.add(map);
        }

        //存放到request域中
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("orderMode",orderMode);
        return "/index";
    }

    /**
     * 搜索
     * @param model
     * @param page
     * @param content
     * @return
     */
    @PostMapping("/search")
    public String searchPosts(Model model,Page page,String content){
        if (StringUtils.isBlank(content)){
            //搜索为空直接返回首页
            return "redirect:/index";
        }

        //封装这个page
        page.setRows(postService.searchDiscussPostRows(content));   //封装总条数
        page.setPath("/search");

        //查询满足条件的帖子
        List<DiscussPost> list = postService.searchDiscussPosts(content,page.getOffset(), page.getLimit());
        //封装数据
        List<Map<String,Object>> discussPosts = new ArrayList<Map<String,Object>>();
        for (DiscussPost discussPost : list) {
            //根据每个帖子的用户id关联查询用户
            Map<String,Object> map = new HashMap<>();
            User user = userService.findUserById(discussPost.getUserId());
            map.put("user",user);    //帖子的作者
            map.put("post",discussPost);
            long likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussPost.getId());
            map.put("likeCount",likeCount);
            discussPosts.add(map);
        }

        //存放到request域中
        model.addAttribute("discussPosts",discussPosts);
        model.addAttribute("content",content);

        return "site/search";
    }

    /**
     * 报错跳转页面
     * @return
     */
    @GetMapping("/error")
    public String error(){
        return "/error/500";
    }
}
