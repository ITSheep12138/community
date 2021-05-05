package com.community.service;

import com.community.bean.LoginTicket;
import com.community.bean.User;
import com.community.dao.LoginTicketMapper;
import com.community.dao.UserMapper;
import com.community.utils.CommunityConstant;
import com.community.utils.CommunityUtil;
import com.community.utils.MailClient;
import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户相关的操作逻辑
 */
@Service
public class UserService {

    @Autowired(required = false)
    UserMapper userMapper;
    @Autowired
    MailClient mailClient;
    @Autowired
    TemplateEngine templateEngine;
    @Autowired
    RedisTemplate redisTemplate;

//    @Autowired(required = false)
//    LoginTicketMapper LoginTicketMapper;

    //注入定义好的数据
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 根据用户id查找用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    public User findUserById(int userId){
//        return userMapper.selectById(userId);
        User user = this.getUserFromRedisById(userId);
        if (user == null){
            user = this.initCache(userId);
        }
        return user;
    }

    /**
     * 注册用户逻辑
     * @param user 注册的用户信息
     * @return 注册结果信息
     */
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        //首先是校验用户信息
        if(user == null){
            throw new IllegalArgumentException("用户信息不能为空");
        }
        //然后根据这些信息查询数据库，校验数据是否有效
        User u = userMapper.selectByName(user.getUsername());
        if(u != null){
            map.put("usernameMsg","该用户名已存在！");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null){
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }

        //数据都合法,填充数据
        user.setSalt(CommunityUtil.generate().substring(0,5));
        user.setStatus(0);
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(CommunityUtil.generate());
        user.setCreateTime(new Date());
        user.setType(0);
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png",(int)(Math.random()*1000)));
        userMapper.insertUser(user);

        //然后发送邮件激活这个账号
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //url为每一个用户激活的标识，需要指定这个用户的id和激活码
        //http://localhost:8080/community/activation/{id}/{code}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url",url);
        String process = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(),"激活账号",process);

        return map;
    }

    /**
     * 用于激活账号
     * @param userId 需要被激活的账号
     * @param code  激活码
     * @return 激活信息
     */
    public int activation(int userId,String code){
        //激活账号
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1){
            return CommunityConstant.ACTIVATION_REPEAT;
        } else if (!user.getActivationCode().equals(code)){
            return CommunityConstant.ACTIVATION_FAILURE;
        } else{
//            userMapper.updateStatus(userId,1);
            this.deleteUser(userId);    //清理缓存
            return CommunityConstant.ACTIVATION_SUCCESS;
        }
    }

    /**
     * 用户登录逻辑，登入之后生成凭证插入redis数据库
     * @param username
     * @param password
     * @param expired
     * @return
     */
    public Map<String,Object> login(String username,String password,int expired){
        Map<String,Object> map = new HashMap<>();
        //校验用户名和密码是否为空
        if (!StringUtils.hasLength(username)){
            map.put("usernameMsg","用户名不能为空！");
            return map;
        }
        if (!StringUtils.hasLength(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        //根据用户名密码查询
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","用户名不存在！");
            return map;
        }
        if(user.getStatus() == 0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }
        password = CommunityUtil.md5(password+user.getSalt());

        if(!password.equals(user.getPassword())){
            map.put("passwordMsg","密码错误！");
            return map;
        }

        //都正确的话就生成凭证信息
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generate());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expired*1000));

        //LoginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }

    /**
     * 根据凭证退出登录
     * @param ticket
     */
    public void logout(String ticket){
        //LoginTicketMapper.updateTicketByTicket(ticket,1);
        String ticketKey = RedisUtil.getTicketKey(ticket);
        LoginTicket o = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        o.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,o);
    }

    /**
     * 根据ticket查询对应的凭证信息
     * @param ticket
     */
    public LoginTicket getLoginTicket(String ticket){
        //return LoginTicketMapper.selectTicketByTicket(ticket);
        String ticketKey = RedisUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }

    /**
     * 通过名字查询
     * @param username
     * @return
     */
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    /**
     * 更新头像
     * @param id
     * @param headerUrl
     */
    public int updateHeader(int id, String headerUrl) {
        int i = userMapper.updateHeader(id, headerUrl);
        this.deleteUser(id);
        return i;
    }


    //优先从缓存中取
    private User getUserFromRedisById(int userId){
        String userKey = RedisUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    //如果缓存中没有，则存入redis
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user,1, TimeUnit.HOURS);
        return user;
    }

    //如果缓存中没有，则存入redis
    private void deleteUser(int userId){
        String userKey = RedisUtil.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
}
