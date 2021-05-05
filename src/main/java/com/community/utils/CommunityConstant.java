package com.community.utils;

/**
 * 用于定义常量
 */
public interface CommunityConstant {
    //注册激活页面常量，0：成功，1：账号已激活，2：激活码错误
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    //记住我按钮设定的cookie的保存时间
    int DEFAULT_EXPIRED = 60*60;
    int MAX_EXPIRED = 60*60*24;

    //实体的类型
    int ENTITY_TYPE_POST = 1;   //帖子的评论
    int ENTITY_TYPE_REPLAY = 2; //回复：评论的评论
    int ENTITY_TYPE_USER = 3; //用户
    int SYSTEM_USER = 1; //用户
}
