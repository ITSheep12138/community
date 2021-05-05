package com.community.dao;

import com.community.bean.User;

public interface UserMapper {

    //更具用户id查询用户
    User selectById(int id);

    //根据用户名称查询
    User selectByName(String username);

    //根据用户邮箱查询
    User selectByEmail(String email);

    //插入用户
    void insertUser(User user);

    //更新用户信息
    void updateStatus(int id,int status);
    int updateHeader(int id,String headerUrl);
    void updatePassword(int id,String password);
}
