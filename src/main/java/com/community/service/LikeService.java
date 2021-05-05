package com.community.service;

import com.community.utils.RedisUtil;
import com.sun.org.apache.bcel.internal.generic.RETURN;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.unbescape.css.CssIdentifierEscapeLevel;

/**
 * 点赞业务，主要操作Redis
 */
@Service
@SuppressWarnings("ALL")
public class LikeService {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    /**
     * 点赞业务，
     * @param userId 用于放入set集合
     * @param entityType
     * @param entityId
     * @param entityUserId 被点赞的用户id
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){
        //点赞需要给被点赞的这个用户累加点赞数量,所以需要使用redis中的事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisUtil.getUserLikeKey(entityUserId);
                boolean member = redisOperations.opsForSet().isMember(entityLikeKey,userId);
                redisOperations.multi();    //开启事务
                if (member){
                    //说明有这个点赞，那我再次点击就取消这个赞
                    redisOperations.opsForSet().remove(entityLikeKey,userId);
                    redisOperations.opsForValue().decrement(userLikeKey);
                }else{
                    //没有点过赞，存入这个集合
                    redisTemplate.opsForSet().add(entityLikeKey,userId);
                    redisOperations.opsForValue().increment(userLikeKey);
                }

                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询点赞的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long getLikeCount(int entityType,int entityId){
        //首先需要判断点没点过赞，判断集合中是否有这个userID即可
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某个人的点赞状态，也就是我对哪些数据点过赞了。
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        //首先需要判断点没点过赞，判断集合中是否有这个userID即可
        String entityLikeKey = RedisUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId) == true ? 1 : 0;
    }

    /**
     * 查询这个用户获得的赞的数量
     * @param userId
     * @return
     */
    public int getUserCount(int userId){
        String userLikeKey = RedisUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count;
    }
}
