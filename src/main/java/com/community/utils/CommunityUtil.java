package com.community.utils;

import com.alibaba.fastjson.JSONObject;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.rmi.MarshalledObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 整个项目的自定义工具，用于处理字符串
 */
public class CommunityUtil {

    /**
     * 利用生成一个随机的字符串
     * @return 返回一个随机字符串
     */
    public static String generate(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密
     * @param key 目标值
     * @return 加密后的值
     */
    public static String md5(String key){
        return StringUtils.hasLength(key) ? DigestUtils.md5DigestAsHex(key.getBytes()) : null;
    }

    /**
     * 将数据装换成JSON格式的字符串
     * @param code
     * @param msg
     * @return
     */
    public static String getJSONString(int code, String msg, Map<String,Object> map){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code",code);
        jsonObject.put("msg",msg);
        if (map != null){
            //将map类型数据封装成JSON
            for (String key : map.keySet()) {
                jsonObject.put(key,map.get(key));
            }
        }
        return jsonObject.toJSONString();
    }

    /**
     * 重载方法
     * @param code
     * @param msg
     * @return
     */
    public static String getJSONString(int code, String msg){
        return getJSONString(code,msg,null);
    }
    public static String getJSONString(int code){
        return getJSONString(code,null,null);
    }

    public static void main(String[] args) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("name","张三");
        System.out.println(CommunityUtil.getJSONString(0,"测试数据",map));
        System.out.println();
    }

}
