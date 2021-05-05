package com.community.controller;

import com.community.bean.User;
import com.community.dao.UserMapper;
import com.community.config.myannotation.LoginCheck;
import com.community.service.FollowService;
import com.community.service.LikeService;
import com.community.service.UserService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * 登录用户相关操作
 */
@Controller
@RequestMapping("/user")
public class UserController {

    private Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired(required = false)
    private UserMapper userMapper;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domain;
    @Value("${community.path.upload}")
    private String upload;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 跳转到设置页面
     * @return
     */
    @LoginCheck
    @GetMapping("/setting")
    public String toSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传文件
     * @param model
     * @param headerImage
     * @return
     */
    @LoginCheck
    @PostMapping("/upload")
    public String upload(Model model, MultipartFile headerImage){
        //获取文件各种属性
        if (headerImage != null) {
            String filename = headerImage.getOriginalFilename();
            String suffix = filename.substring(filename.lastIndexOf("."));
            if (suffix == null || suffix.isEmpty()){
                model.addAttribute("msg","貌似不支持这种格式的文件！");
                return "/site/setting";
            }
            //生成图片的名字
            filename = CommunityUtil.generate()+suffix;
            File file = new File(upload +"/"+ filename);
            try {
                headerImage.transferTo(file);
            } catch (IOException e) {
                logger.error("文件上传失败！"+e.getMessage());
                throw new RuntimeException("文件上传失败！",e);
            }

            //更新用户头像,应该是一个web的访问路径：http://localhost:8080/community/user/header/xxx.yyy
            String headerUrl = domain + contextPath + "/user/header/" + filename;
            userService.updateHeader(hostHolder.getUser().getId(),headerUrl);
            return "redirect:/index";
        }

        model.addAttribute("msg","未选择文件！");
        return "/site/setting";
    }


    /**
     * 获取用户头像
     * @param filename
     * @param response
     */
    @GetMapping("/header/{filename}")
    public void getHeader(@PathVariable String filename,HttpServletResponse response){
        //解析这个fileName
        filename = upload + "/" + filename;
        String suffix = filename.substring(filename.lastIndexOf("."));
        //文件IO,java7新语法，可以自动关闭这个流
        try (
            OutputStream outputStream = response.getOutputStream();
            InputStream inputStream = new FileInputStream(filename);
        ){
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,length);
            }
        } catch (IOException e) {
            logger.error("获取文件错误："+e.getMessage());
        }
    }

    /**
     * 更改密码
     * @param model
     * @param oldpassword
     * @param newpassword
     * @return
     */
    @LoginCheck
    @PostMapping("/changePassword")
    public String changePassword(Model model, String oldpassword, String newpassword,
                                 @CookieValue("ticket") String ticket){
        if (oldpassword.equals(newpassword)){
            model.addAttribute("passwordMsg","新密码不能和旧密码一样！");
            return "/site/setting";
        }
        //检查旧密码是否正确
        User user = userMapper.selectById(hostHolder.getUser().getId());
        oldpassword = CommunityUtil.md5(oldpassword+user.getSalt());
        if (!oldpassword.equals(user.getPassword())){
            model.addAttribute("passwordMsg","旧密码错误！");
            return "/site/setting";
        }
        newpassword = CommunityUtil.md5(newpassword + user.getSalt());
        //然后更新这个密码
        userMapper.updatePassword(user.getId(),newpassword);
        //删除现有登录的用户
        userService.logout(ticket);

        return "redirect:/login";
    }

    /**
     * 详情页面
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        //查询点赞数量
        int likeCount = likeService.getUserCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId, CommunityConstant.ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);
        //粉丝数量
        long followerCount = followService.findFollowerCount(CommunityConstant.ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);
        //是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser() != null){
            hasFollowed = followService.hasFollow(hostHolder.getUser().getId(), CommunityConstant.ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "site/profile";
    }
}
