package com.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.community.bean.Message;
import com.community.bean.Page;
import com.community.bean.User;
import com.community.config.mq.MqConfig;
import com.community.config.myannotation.LoginCheck;
import com.community.service.MessageService;
import com.community.service.UserService;
import com.community.utils.CommunityUtil;
import com.community.utils.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.HtmlUtils;

import java.util.*;

/**
 * 私心的消息接口
 */
@Controller
public class MessageController {

    @Autowired
    private MessageService messageService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;

    /**
     * 分页查询所有的消息
     * @param model
     * @param page
     * @return
     */
    @LoginCheck
    @GetMapping("/letter/list")
    public String getLettersPage(Model model, Page page){
        //设置分页信息
        page.setPath("/letter/list");
        page.setRows(messageService.getMessageCount(hostHolder.getUser().getId()));
        page.setLimit(5);

        //然后查询所有的首页的信息
        List<Message> messageList = messageService.getMessageList(
                hostHolder.getUser().getId(), page.getOffset(), page.getLimit());
        //封装数据
        List<Map<String,Object>> list = new ArrayList<>();
        if (messageList != null){
            for (Message message : messageList) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("conversation",message);
                //每一个会话的未读消息
                map.put("unReadCount",messageService.getLetterCountUnRead(
                        hostHolder.getUser().getId(),message.getConversationId()));
                //每一个会话的所有消息
                map.put("letterCount",messageService.getLetterCount(message.getConversationId()));

                //获取目标对象
                int targetId = hostHolder.getUser().getId() == message.getFromId() ?  message.getToId() : message.getFromId();
                User user = userService.findUserById(targetId);
                map.put("target",user);
                list.add(map);
            }
        }
        model.addAttribute("conversations",list);

        //查询未读消息数量
        int letterCountUnRead = messageService.getLetterCountUnRead(hostHolder.getUser().getId(), null);
        model.addAttribute("letterUnReadCount",letterCountUnRead);
        //查询系统未读数量
        int unReadNoticeCount = messageService.getUnReadNoticeCount(hostHolder.getUser().getId(), null);
        model.addAttribute("unReadNoticeCount",unReadNoticeCount);


        List<Integer> ids = getLettersIds(messageList);
        if (!ids.isEmpty()){
            int i = messageService.updateMessage(ids);
        }

        return "/site/letter";
    }

    /**
     * 获取未读消息的id
     * @param messageList
     * @return
     */
    private List<Integer> getLettersIds(List<Message> messageList) {
        List<Integer> messages = new ArrayList<>();
        if(messageList != null){
            for (Message message : messageList) {
                if (message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0){
                    messages.add(message.getId());
                }
            }
        }
        return messages;
    }

    /**
     * 分页查询消息列表
     * @param model
     * @param page
     * @param conversationId
     * @return
     */
    @LoginCheck
    @GetMapping("/letter/detail/{conversationId}")
    public String getConversationPage(Model model, Page page,
                                      @PathVariable("conversationId") String conversationId){

        //设置分页信息
        page.setPath("/letter/detail/"+conversationId);
        page.setRows(messageService.getMessageCount(hostHolder.getUser().getId()));
        page.setLimit(5);
        //根据ConversationId 查询这个会话的详情信息
        List<Message> letters = messageService.getLetters(conversationId, (page.getOffset()), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        for (Message letter : letters) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("letter",letter);
            map.put("fromUser",userService.findUserById(letter.getFromId()));

            list.add(map);
        }
        model.addAttribute("letters",list);
        model.addAttribute("target",getLetterTarget(conversationId));
        return "/site/letter-detail";
    }

    /**
     * 查询私信的发送者
     * @param conversationId
     * @return
     */
    private User getLetterTarget(String conversationId) {
        String[] split = conversationId.split("_");
        int id0 = Integer.parseInt(split[0]);
        int id1 = Integer.parseInt(split[1]);
        return userService.findUserById(hostHolder.getUser().getId() == id0 ? id1 : id0);
    }

    /**
     * 发送私信
     * @param toName
     * @param content
     * @return
     */
    @LoginCheck
    @PostMapping("/letter/add")
    @ResponseBody
    public String addMessage(String toName,String content){
        //通过用户名找到这个User
        User user = userService.findUserByName(toName);
        if (user == null){
            return CommunityUtil.getJSONString(1,"这个用户貌似不存在了");
        }

        //然后就是补充数据
        Message message = new Message();
        message.setContent(content);
        message.setCreateTime(new Date());
        message.setToId(user.getId());
        if (hostHolder.getUser().getId() < user.getId()){
            message.setConversationId(hostHolder.getUser().getId()+"_"+user.getId());
        } else {
            message.setConversationId(user.getId()+"_"+hostHolder.getUser().getId());
        }
        message.setFromId(hostHolder.getUser().getId());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0,"发送成功！");
    }

    /**
     * 系统通知接口
     * @param model
     * @return
     */
    @LoginCheck
    @GetMapping("/notice/list")
    public String getNoticeList(Model model){
        User user = hostHolder.getUser();

        //查询评论类的通知
        Message message = messageService.getLatestNotice(user.getId(), MqConfig.QUEUE_COMMENT);
        Map<String,Object> messageVO = new HashMap<>();
        if (message != null){
            //将数据恢复
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map data = JSONObject.parseObject(content, Map.class);
            messageVO.put("message",message);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("postId",data.get("postId"));

            int noticeCount = messageService.getNoticeCount(user.getId(), MqConfig.QUEUE_COMMENT);
            messageVO.put("count",noticeCount);
            int unNoticeCount = messageService.getUnReadNoticeCount(user.getId(), MqConfig.QUEUE_COMMENT);
            messageVO.put("unRead",unNoticeCount);
        }
        model.addAttribute("commentNotice",messageVO);

        //查询点赞类的通知
        message = messageService.getLatestNotice(user.getId(), MqConfig.QUEUE_LIKE);
        messageVO = new HashMap<>();
        if (message != null){
            //将数据恢复
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map data = JSONObject.parseObject(content, Map.class);
            messageVO.put("message",message);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("postId",data.get("postId"));

            int noticeCount = messageService.getNoticeCount(user.getId(), MqConfig.QUEUE_LIKE);
            messageVO.put("count",noticeCount);
            int unNoticeCount = messageService.getUnReadNoticeCount(user.getId(), MqConfig.QUEUE_LIKE);
            messageVO.put("unRead",unNoticeCount);
        }
        model.addAttribute("likeNotice",messageVO);

        //查询关注类的通知
        message = messageService.getLatestNotice(user.getId(), MqConfig.QUEUE_FOLLOW);
        messageVO = new HashMap<>();
        if (message != null){
            //将数据恢复
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map data = JSONObject.parseObject(content, Map.class);
            messageVO.put("message",message);
            messageVO.put("user",userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityId",data.get("entityId"));
            messageVO.put("entityType",data.get("entityType"));
            messageVO.put("postId",data.get("postId"));

            int noticeCount = messageService.getNoticeCount(user.getId(), MqConfig.QUEUE_FOLLOW);
            messageVO.put("count",noticeCount);
            int unNoticeCount = messageService.getUnReadNoticeCount(user.getId(), MqConfig.QUEUE_FOLLOW);
            messageVO.put("unRead",unNoticeCount);
        }
        model.addAttribute("followNotice",messageVO);

        //查询系统未读数量
        int unReadNoticeCount = messageService.getUnReadNoticeCount(user.getId(), null);
        model.addAttribute("unReadNoticeCount",unReadNoticeCount);
        //查询朋友未读
        int letterCountUnRead = messageService.getLetterCountUnRead(user.getId(), null);
        model.addAttribute("letterCountUnRead",letterCountUnRead);

        return "site/notice";
    }

    /**
     * 某个系统通知的具体数据
     * @param model
     * @param page
     * @param topic
     * @return
     */
    @LoginCheck
    @GetMapping("/notice/detail/{topic}")
    public String getNoticePage(Model model, Page page,
                                      @PathVariable("topic") String topic){
        //设置分页信息
        page.setPath("/notice/detail/"+topic);
        page.setRows(messageService.getNoticeCount(hostHolder.getUser().getId(),topic));
        page.setLimit(5);
        //topic 查询这个会话的详情信息
        List<Message> notices = messageService.getNoticeDetailPage(hostHolder.getUser().getId(),topic, (page.getOffset()), page.getLimit());
        List<Map<String,Object>> list = new ArrayList<>();
        for (Message notice : notices) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("notice",notice);
            map.put("fromUser",userService.findUserById(notice.getFromId()));
            String content = HtmlUtils.htmlUnescape(notice.getContent());
            Map data = JSONObject.parseObject(content, Map.class);
            map.put("user",userService.findUserById((Integer) data.get("userId")));
            map.put("entityId",data.get("entityId"));
            map.put("entityType",data.get("entityType"));
            map.put("postId",data.get("postId"));

            list.add(map);
        }
        model.addAttribute("notices",list);
        List<Integer> lettersIds = getLettersIds(notices);
        if (!lettersIds.isEmpty()){
            messageService.updateMessage(lettersIds);
        }

        return "site/notice-detail";
    }
}
