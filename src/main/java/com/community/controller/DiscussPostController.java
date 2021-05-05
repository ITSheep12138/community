package com.community.controller;

import com.community.bean.Comment;
import com.community.bean.DiscussPost;
import com.community.bean.Page;
import com.community.bean.User;
import com.community.config.myannotation.LoginCheck;
import com.community.service.CommentService;
import com.community.service.DiscussPostService;
import com.community.service.LikeService;
import com.community.service.UserService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 帖子相关接口
 */
@Controller
@RequestMapping("/discussPost")
public class DiscussPostController {

    @Autowired
    private DiscussPostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private LikeService likeService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发布帖子
     * @return
     */
    @LoginCheck
    @PostMapping("/add")
    @ResponseBody
    public String addDiscussPost(String title,String content){
        //判断有没有登陆
        User user = hostHolder.getUser();
        if(user == null){
            return CommunityUtil.getJSONString(403,"请先登录在发布！");
        }

        //填充数据
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPost.setStatus(0);
        discussPost.setType(0);

        postService.insertDiscussPost(discussPost);

        //计算帖子的分数
        String postScoreKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,discussPost.getId());

        //异常后续统一处理
        return CommunityUtil.getJSONString(0,"发布成功！");
    }

    /**
     * 查询帖子详情
     * @param discussId
     * @param model
     * @return
     */
    @GetMapping("/detail/{discussId}")
    public String selectDiscussPostById(@PathVariable("discussId") int discussId, Model model,Page page){
        //查询帖子的信息
        DiscussPost discussPost = postService.selectDiscussPostById(discussId);
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("discussPost",discussPost);
        model.addAttribute("user",user);

        //此时需要查询当前这个帖子的评论
        page.setLimit(5);    //评论的数量，每页
        page.setPath("/discussPost/detail/"+discussId);
        page.setRows(discussPost.getCommentCount());

        //帖子的点赞数量和用户是否对这个帖子点赞
        long likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_POST, discussId);
        model.addAttribute("likeCount",likeCount);
        int status = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_POST, discussId);
        model.addAttribute("status",status);

        //根据帖子的id查询分页查询评论
        List<Comment> comments = commentService.selectCommentsPage(CommunityConstant.ENTITY_TYPE_POST,
                discussId, page.getOffset(), page.getLimit());
        //这个类型用于封装每一个评论的其他信息
        List<Map<String,Object>> commentVoList = new ArrayList<Map<String,Object>>();
        if(comments != null){
            for (Comment comment : comments) {
                HashMap<String, Object> commentVo = new HashMap<>();
                //根据这个评论查询出评论的人
                commentVo.put("user",userService.findUserById(comment.getUserId()));
                commentVo.put("comment",comment);   //该评论信息

                //帖子的点赞数量和用户是否对这个评论点赞
                likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_REPLAY, comment.getId());
                commentVo.put("likeCount",likeCount);
                status = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_REPLAY, comment.getId());
                commentVo.put("status",status);

                //查询评论的评论
                List<Comment> replayList = commentService.selectCommentsPage(CommunityConstant.ENTITY_TYPE_REPLAY,
                        comment.getId(), 0, Integer.MAX_VALUE);
                //封装每一条评论的信息
                List<Map<String,Object>> replayVoList = new ArrayList<Map<String,Object>>();
                if (replayList != null){
                    for (Comment replay : replayList) {
                        HashMap<String, Object> replayVo = new HashMap<>();
                        //回复着自己的信息
                        replayVo.put("replay",replay);
                        replayVo.put("user",userService.findUserById(replay.getUserId()));

                        //帖子的点赞数量和用户是否对这个评论点赞
                        likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_REPLAY, replay.getId());
                        replayVo.put("likeCount",likeCount);
                        status = likeService.findEntityLikeStatus(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_REPLAY, replay.getId());
                        replayVo.put("status",status);

                        //被回复的人的信息
                        User target = replay.getTargetId() == 0 ? null:userService.findUserById(replay.getUserId());
                        replayVo.put("target",target);
                        replayVoList.add(replayVo);
                    }
                }
                //每一条评论的所有回复信息
                commentVo.put("replays",replayVoList);
                commentVo.put("replayCount",commentService.selectCount(CommunityConstant.ENTITY_TYPE_REPLAY,comment.getId()));

                commentVoList.add(commentVo);
            }

            model.addAttribute("comments",commentVoList);
        }


        return "/site/discuss-detail";
    }

    /**
     * 设置帖子置顶
     * @param id
     * @return
     */
    @LoginCheck
    @PostMapping("/top")
    @ResponseBody
    public String setTop(int id){
        postService.updateType(id,1);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 设置帖子精华
     * @param id
     * @return
     */
    @LoginCheck
    @PostMapping("/wonderful")
    @ResponseBody
    public String setWonderful(int id){
        postService.updateStatus(id,1);
        //计算帖子的分数
        String postScoreKey = RedisUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(postScoreKey,id);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * 删除帖子
     * @param id
     * @return
     */
    @LoginCheck
    @PostMapping("/delete")
    @ResponseBody
    public String setDelete(int id){
        postService.updateStatus(id,2);
        return CommunityUtil.getJSONString(0);
    }
}
