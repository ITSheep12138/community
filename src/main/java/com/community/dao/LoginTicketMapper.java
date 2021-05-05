package com.community.dao;

import com.community.bean.LoginTicket;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 初始登录校验版本
 * @apiNote 此API再引入Redis之后废弃使用
 */
@Deprecated
public interface LoginTicketMapper {

    /**
     * 插入凭证信息
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id,ticket,status,expired) ",
            "values(#{userId},#{ticket},#{status},#{expired})"
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据凭证信息获取所有信息
     * @param ticket
     * @return
     */
    @Select({
            "select * from login_ticket where ticket=#{ticket}"
    })
    LoginTicket selectTicketByTicket(String ticket);

    /**
     * 逻辑删除凭证信息
     * @param ticket
     * @param status
     * @return
     */
    @Update({
            "update login_ticket set status=#{status} where ticket = #{ticket}"
    })
    int updateTicketByTicket(String ticket,int status);
}
