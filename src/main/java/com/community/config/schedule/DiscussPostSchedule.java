package com.community.config.schedule;

import com.community.bean.DiscussPost;
import com.community.service.DiscussPostService;
import com.community.service.LikeService;
import com.community.utils.CommunityConstant;
import com.community.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 定时执行，更新帖子热度
 */
@Component
@EnableScheduling
public class DiscussPostSchedule {

    private Logger logger = LoggerFactory.getLogger(DiscussPostSchedule.class);
    //网站创建的时间
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("epoch 时间错误"+e.getMessage());
        }
    }


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService postService;
    @Autowired
    private LikeService likeService;


    /**
     * 更新帖子的分数
     * fixedRate = 1000 * 60 * 60 表示每一个小时执行一次
     */
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void updatePostScore(){
        String postScoreKey = RedisUtil.getPostScoreKey();
        BoundSetOperations boundSetOps = redisTemplate.boundSetOps(postScoreKey);

        //boundSetOps是一个集合，绑定所有的这个key的数据
        if (boundSetOps.size() == 0){
            logger.info("【任务取消】 没有需要更新的帖子");
            return;
        }

        logger.info("【任务开始】 正在更新的帖子分数");
        while (boundSetOps.size() > 0){
            //计算这些帖子的分数
            this.refresh((Integer) boundSetOps.pop());
        }
        logger.info("【任务结束】 帖子分数更新完成");

    }

    private void refresh(Integer id) {
        DiscussPost discussPost = postService.selectDiscussPostById(id);
        if (discussPost == null) {
            logger.info("该帖子不存在！");
            return;
        }

        boolean wonderful = discussPost.getStatus() == 1;
        int commentCount = discussPost.getCommentCount();
        long likeCount = likeService.getLikeCount(CommunityConstant.ENTITY_TYPE_POST,discussPost.getId());

        //权重公式
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //计算分数
        double score = Math.log10(Math.max(w,1)) +
                (discussPost.getCreateTime().getTime()-epoch.getTime())/(1000*3600*24);

        //更新分数
        postService.updateScore(discussPost.getId(),score);
    }

}
