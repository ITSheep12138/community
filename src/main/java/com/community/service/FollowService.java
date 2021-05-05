package com.community.service;

import com.community.utils.CommunityConstant;
import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import sun.awt.image.IntegerInterleavedRaster;

import java.util.*;

/**
 * 关注业务的service
 */
@Service
public class FollowService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserService userService;


    /**
     * 关注方法
     * @param userId 点击关注的人
     * @param entityType 关注的对象的类型，可以是人或者帖子
     * @param entityId  被关注的实体
     */
    public void follow(int userId,int entityType,int entityId){
        //同样，关注需要设计两个操作，是否关注，关注着的数据量累加
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //获取KEY
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType);

                redisOperations.multi();
                redisOperations.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisOperations.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return redisOperations.exec();
            }
        });
    }

    /**
     * 取关方法
     * @param userId 点击关注的人
     * @param entityType 关注的对象的类型，可以是人或者帖子
     * @param entityId  被关注的实体
     */
    public void unFollow(int userId,int entityType,int entityId){
        //同样，关注需要设计两个操作，是否关注，关注着的数据量累加
        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                //获取KEY
                String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
                String followerKey = RedisUtil.getFollowerKey(entityId, entityType);

                redisOperations.multi();
                redisOperations.opsForZSet().remove(followeeKey, entityId);
                redisOperations.opsForZSet().remove(followerKey, userId);
                return redisOperations.exec();
            }
        });
    }

    /**
     * 查询某个实体被其他多少实体关注了
     * @param userId
     * @param entityType
     * @return
     */
    public long findFolloweeCount(int userId,int entityType){
        //获取KEY
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return  redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查询某个实体被多少人关注
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowerCount(int entityType,int entityId){
        //获取KEY
        String followeeKey = RedisUtil.getFollowerKey(entityId, entityType);
        return  redisTemplate.opsForZSet().zCard(followeeKey);
    }

    /**
     * 查看某个人是否关注了某个实体
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollow(int userId,int entityType,int entityId){
        String followeeKey = RedisUtil.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey,entityId) != null;
    }

    /**
     * 查询某个用户关注的人的列表
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String,Object>> getFollowees(int userId,int offset,int limit){
        //获取KEY
        String followeeKey = RedisUtil.getFolloweeKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (targetIds == null) return null;

        //封装数据,待优化
        List<Map<String,Object>> maps = new ArrayList<>();
        for (Object targetId : targetIds) {
            Map<String,Object> map = new HashMap<>();
            map.put("user",userService.findUserById((Integer) targetId));
            Double score = redisTemplate.opsForZSet().score(followeeKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            maps.add(map);
        }

        return maps;
    }

    /**
     * 查询某个用户的粉丝的列表
     * @param userId
     * @param offset
     * @param limit
     * @returnr
     */
    public List<Map<String,Object>> getFollowers(int userId,int offset,int limit){
        //获取KEY
        String followerKey = RedisUtil.getFollowerKey(userId, CommunityConstant.ENTITY_TYPE_USER);
        Set<Object> targetIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (targetIds == null) return null;

        //封装数据,待优化
        List<Map<String,Object>> maps = new ArrayList<>();
        for (Object targetId : targetIds) {
            Map<String,Object> map = new HashMap<>();
            map.put("user",userService.findUserById((Integer) targetId));
            Double score = redisTemplate.opsForZSet().score(followerKey, targetId);
            map.put("followTime",new Date(score.longValue()));
            maps.add(map);
        }

        return maps;
    }

}
