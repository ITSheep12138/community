package com.community.utils;


/**
 * Redis相关工具
 * 说明：
 *    在redis中存放的帖子点赞数据格式为：like_entity_entityType_entityType  set(存放的是给帖子点赞的userId)
 *    用户的赞的数据格式为：like_user_userId
 *
 */
public class RedisUtil {

    //定义KEY的名字
    private static final String SPLIT = "_";
    private static final String PREFIX_ENTITY_LIKE = "like_entity";
    private static final String PREFIX_USER_LIKE = "like_user";
    private static final String PREFIX_FOLLOWER = "follower";   //关注
    private static final String PREFIX_FOLLOWEE = "followee";   //被关注
    private static final String PREFIX_CAPTCHA = "captcha";   //验证码
    private static final String PREFIX_TICKET = "ticket";   //验证码
    private static final String PREFIX_USER = "user";   //登录的用户
    private static final String PREFIX_UV = "uv";   //网站访问数量
    private static final String PREFIX_DAU = "dau";   //登录用户访问数量
    private static final String PREFIX_POST = "post";   //登录用户访问数量

    /**
     * 获取这个帖子的Key的存在格式
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    /**
     * 获取这个用户的Key的存在格式
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    /**
     * 某个用户关注的实体,存放的是被关注的id以及一个分数，这个分数就是当前时间。
     * @return PREFIX_FOLLOWEE_userId_entityType -> zset(entityId,now)
     */
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个用户拥有的粉丝
     * @return PREFIX_FOLLOWER_entityType_entityId -> zset(userId,now)
     */
    public static String getFollowerKey(int entityId,int entityType){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 获取验证码
     * @param owner
     * @return
     */
    public static String getCaptchaKey(String owner){
        return PREFIX_CAPTCHA + SPLIT + owner;
    }

    /**
     * 获取登陆凭证
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 获取登录用户的key
     * @param userId
     * @return
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 单日uv
     * @param date
     * @return
     */
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 统计某个区间的UV
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日DAU
     * @param date
     * @return
     */
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 统计某个区间的DAU
     * @param startDate
     * @param endDate
     * @return
     */
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 帖子分数
     * @return
     */
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }
}
