package com.community.dao;

import com.community.bean.DiscussPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 核心模块，主要用于查询帖子
 */
public interface DiscussPostMapper {

    /**
     * 分页查询帖子,这是一个个人主页和首页查询合并地方法
     * @param userId  用于个人首页查询
     * @param offset  页码
     * @param limit   每页条数
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit,int orderMode);

    /**
     * 用于查询帖子的数量，注:这里的@Param("userId")是必须要写的，当只有一个参数的时候，需要用到动态SQL就一定要写这个
     * @param userId  用于个人首页查询
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);

    /**
     * 插入帖子
     * @param discussPost 封装的帖子数据
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);

    /**
     * 查询帖子详情
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

    /**
     * 更新帖子的评论数量
     * @param id
     * @param commentCount
     * @return
     */
    void updateCommentCount(int id,int commentCount);

    /**
     * 置顶帖子，更新帖子的状态
     * @param id
     * @param type
     * @return
     */
    int updateType(int id,int type);

    /**
     * 精华帖子，更新帖子的状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(int id,int status);

    /**
     * 更新帖子分数
     * @param id
     * @param score
     */
    void updateScore(int id, double score);

    /**
     * 根据条件查询一共有多少条数据
     * @param content
     * @return
     */
    int searchDiscussPostRows(String content);

    /**
     * 搜索
     * @param offset
     * @param limit
     * @return
     */
    List<DiscussPost> searchDiscussPosts(String content,int offset, int limit);
}
