package com.community.service;

import com.community.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 数据统计
 */
@Service
public class DateService {

    @Autowired
    RedisTemplate redisTemplate;

    private SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    /**
     * 记录这个IP今天的访问次数，一个IP多次访问算一次
     * @param ip
     */
    public void recordUV(String ip){
        String uvKey = RedisUtil.getUVKey(format.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }

    /**
     * 统计区间UV
     * @param start
     * @param end
     * @return
     */
    public long calculateUV(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("传入的日期为空！");
        }

        //整理这个日期范围的数据，然后做一个合并
        List<String> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);    //设置开始时间
        while (!calendar.getTime().after(end)){
            //calendar.getTime().after(end)表示结束日期在这个之后则为true
            String uvKey = RedisUtil.getUVKey(format.format(calendar.getTime()));
            keyList.add(uvKey);
            //日期后推一天
            calendar.add(Calendar.DATE,1);
        }

        String uvKey = RedisUtil.getUVKey(format.format(start), format.format(end));
        redisTemplate.opsForHyperLogLog().union(uvKey,keyList.toArray());

        //返回统计结果
        return redisTemplate.opsForHyperLogLog().size(uvKey);
    }

    /**
     * 统计登录用户访问
     * @param userId
     */
    public void recordDAU(int userId){
        String dauKey = RedisUtil.getDAUKey(format.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    /**
     * 统计区间DAU
     * @param start
     * @param end
     * @return
     */
    public long calculateDAU(Date start,Date end){
        if (start == null || end == null){
            throw new IllegalArgumentException("传入的日期为空！");
        }

        //整理这个日期范围的数据，然后做一个合并
        List<byte[]> keyList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);    //设置开始时间
        while (!calendar.getTime().after(end)){
            //calendar.getTime().after(end)表示结束日期在这个之后则为true
            String dauKey = RedisUtil.getDAUKey(format.format(calendar.getTime()));
            keyList.add(dauKey.getBytes());
            //日期后推一天
            calendar.add(Calendar.DATE,1);
        }

        //进行OR运算
        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                String dauKey = RedisUtil.getDAUKey(format.format(start), format.format(end));
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        dauKey.getBytes(),keyList.toArray(new byte[0][0]));
                return connection.bitCount(dauKey.getBytes());
            }
        });
    }
}
