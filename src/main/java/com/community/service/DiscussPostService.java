package com.community.service;

import com.community.bean.DiscussPost;
import com.community.dao.DiscussPostMapper;
import com.community.utils.SensitiveFilter;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jdk.nashorn.internal.ir.ReturnNode;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {

    private Logger logger = LoggerFactory.getLogger(DiscussPostService.class);
    @Autowired(required = false)
    DiscussPostMapper postMapper;
    @Autowired
    SensitiveFilter sensitiveFilter;

    /*
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expire-seconds}")
    private int expireSeconds;
    */

    // Caffeine核心接口: Cache, LoadingCache, AsyncLoadingCache

    // 帖子列表缓存
    //private LoadingCache<String, List<DiscussPost>> postListCache;

    // 帖子总数缓存
    //private LoadingCache<Integer, Integer> postRowsCache;

    /*@PostConstruct
    public void init() {
        // 初始化帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if (key == null || key.length() == 0) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        String[] params = key.split(":");
                        if (params == null || params.length != 2) {
                            throw new IllegalArgumentException("参数错误!");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        // 二级缓存: Redis -> mysql

                        logger.debug("load post list from DB.");
                        return postMapper.selectDiscussPosts(0, offset, limit, 1);
                    }
                });
        // 初始化帖子总数缓存
        postRowsCache = Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.debug("load post rows from DB.");
                        return postMapper.selectDiscussPostRows(key);
                    }
                });
    }*/

    /**
     * 分页查询，@Cacheable(value = "posts",condition = "#orderMode==1")启用缓存
     * condition = "#orderMode==1"表示当orderMode为1的时候缓存，也就是只缓存热门帖子
     * @param userId
     * @param offset
     * @param limit
     * @param orderMode 用于分辨首页和热门帖子，0-首页，1-热门
     * @return
     */
    @Cacheable(value = "posts",condition = "#orderMode==1")
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        return postMapper.selectDiscussPosts(userId,offset,limit,orderMode);
    }

    /**
     * 查询所有帖子数量
     */
    @Cacheable(value = "postCount")
    public int findDiscussPostRows(int userId){
        return postMapper.selectDiscussPostRows(userId);
    }

    /**
     * 插入帖子
     * @param discussPost
     * @return
     */
    public int insertDiscussPost(DiscussPost discussPost){
        //校验数据是否为空
        if (discussPost == null){
            throw new IllegalArgumentException("插入帖子数据参数为空");
        }

        //对传进来的数据进行处理
        //防止HTML标签注入
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        //过滤敏感词
        discussPost.setTitle(sensitiveFilter.filterText(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filterText(discussPost.getContent()));

        return postMapper.insertDiscussPost(discussPost);
    }

    /**
     * 查询帖子详情
     * @param id
     * @return
     */
    public DiscussPost selectDiscussPostById(int id){
        return postMapper.selectDiscussPostById(id);
    }

    /**
     * 更新帖子状态
     * @param id
     * @param type
     */
    public int updateType(int id,int type){
        return postMapper.updateType(id, type);
    }
    public int updateStatus(int id,int status){
        return postMapper.updateStatus(id, status);
    }

    /**
     * 更新评论数量
     * @param entityId
     * @param count
     */
    public void updateCommentCount(int entityId, int count) {
        postMapper.updateCommentCount(entityId, count);
    }

    /**
     * 更新帖子分数
     * @param id
     * @param score
     */
    public void updateScore(int id, double score) {
        postMapper.updateScore(id,score);
    }

    /**
     * 根据搜索的条件查询一共有多少条数据
     * @param content
     * @return
     */
    public int searchDiscussPostRows(String content) {
        return postMapper.searchDiscussPostRows(content);
    }

    public List<DiscussPost> searchDiscussPosts(String content,int offset, int limit) {
        return postMapper.searchDiscussPosts(content,offset,limit);
    }
}
