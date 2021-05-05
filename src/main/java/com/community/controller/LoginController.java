package com.community.controller;

import com.community.bean.User;
import com.community.service.UserService;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.RedisUtil;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.wf.captcha.utils.CaptchaUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户登陆注册功能
 */
@Controller
public class LoginController {

    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 跳转到注册页面
     * @return
     */
    @GetMapping("/register")
    public String toRegisterPage(){
        return "/site/register";
    }

    /**
     * 跳转到注册页面
     * @return
     */
    @GetMapping("/login")
    public String toLoginPage(){
        return "/site/login";
    }


    /**
     * 注册未激活
     * @param model 请求与数据
     * @param user 用户提交数据，会自动被springmvc自动注入model
     * @return
     */
    @PostMapping("/register")
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            //为空，说明service中一切正常
            model.addAttribute("msg","注册成功，我们已经向您的邮箱发送了邮件，请尽快激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else{
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }

    /**
     * 激活账号
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @GetMapping("/activation/{userId}/{code}")
    public String activation(Model model, @PathVariable int userId,@PathVariable String code){
        int result = userService.activation(userId, code);
        if(result == CommunityConstant.ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功,账号可以正常使用！");
            model.addAttribute("target","/login");
        } else if (result == CommunityConstant.ACTIVATION_FAILURE){
            model.addAttribute("msg","激活失败,激活码不正确！");
            model.addAttribute("target","/index");
        } else {
            model.addAttribute("msg","该账号已激活，无需重复激活！");
            model.addAttribute("target","/index");
        }

        return "/site/operate-result";
    }

    /**
     * 使用第三方工具生成验证码
     * @param response
     * @throws Exception
     */
    @GetMapping("/captcha")
    public void captcha(HttpServletResponse response) throws Exception {
        // 设置请求头为输出图片类型
        response.setContentType("image/gif");
        response.setHeader("Pragma", "No-cache");
        response.setHeader("Cache-Control", "no-cache");
        response.setDateHeader("Expires", 0);

        // 三个参数分别为宽、高、位数
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        // 设置字体
        specCaptcha.setFont(new Font("Verdana", Font.PLAIN, 32));  // 有默认字体，可以不用设置
        // 设置类型，纯数字、纯字母、字母数字混合
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);
        String captcha = specCaptcha.text().toLowerCase();

        // 验证码存入redis，同时为了辨别每一个用户的验证码，生成一个随机字符串
        String owner = CommunityUtil.generate();
        Cookie cookie = new Cookie("captchaOwner",owner);
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        String captchaKey = RedisUtil.getCaptchaKey(owner);
        //设置验证码最多存放时间为60S
        redisTemplate.opsForValue().set(captchaKey,captcha,60, TimeUnit.SECONDS);

        // 输出图片流
        specCaptcha.out(response.getOutputStream());
    }

    /**
     * 登录接口模块
     * @param username
     * @param password
     * @param code
     * @param rememberme
     * @param model
     * @param captchaOwner
     * @param response
     * @return
     */
    @PostMapping("/login")
    public String login(String username, String password, String code, boolean rememberme,
                        Model model, @CookieValue("captchaOwner") String captchaOwner, HttpServletResponse response){
        String captcha = null;
        //先判断凭证是否失效
        if (StringUtils.isNotBlank(captchaOwner)){
            String captchaKey = RedisUtil.getCaptchaKey(captchaOwner);
            captcha = (String) redisTemplate.opsForValue().get(captchaKey);
        }
        //校验验证码
        if(StringUtils.isBlank(code) || StringUtils.isBlank(captcha) || !captcha.equalsIgnoreCase(code)){
            //重构代码，不再将验证码存入Session
            //CaptchaUtil.clear(request);  // 清除session中的验证码
            model.addAttribute("codeMsg","验证码不正确！");
            return "/site/login";
        }
        //校验账号密码
        int expired = rememberme ? CommunityConstant.DEFAULT_EXPIRED : CommunityConstant.MAX_EXPIRED;
        Map<String, Object> login = userService.login(username, password, expired);
        if(login.containsKey("ticket")){
            //校验成功，派发这个ticket存入cookie
            Cookie cookie = new Cookie("ticket",login.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expired);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            model.addAttribute("usernameMsg",login.get("usernameMsg"));
            model.addAttribute("passwordMsg",login.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 退出登录
     * @param ticket
     * @return
     */
    @GetMapping("/logout")
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        return "/site/login";
    }
}
