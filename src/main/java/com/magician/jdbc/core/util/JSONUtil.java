package com.magician.jdbc.core.util;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * JSON utility class
 */
public class JSONUtil {

    private static Logger logger = LoggerFactory.getLogger(JSONUtil.class);

    /**
     * Convert any object to another java object
     * @param obj
     * @param cls
     * @param <T>
     * @return
     */
    public static  <T> T toJavaObject(Object obj, Class<T> cls) {
        if(obj == null){
            return null;
        }
        try {
            if(obj instanceof String){
                return JSON.parseObject(obj.toString(), cls);
            } else {
                return JSON.parseObject(JSON.toJSONString(obj), cls);
            }
        } catch (Exception e){
            logger.error("JSONUtil toJavaObject error", e);
            return null;
        }
    }

    /**
     * Convert any object to a Map
     * @param obj
     * @return
     */
    public static Map<String, Object> toMap(Object obj) {
        if(obj instanceof Map){
            return (Map<String, Object>) obj;
        }
        Map<String, Object> map = toJavaObject(obj, HashMap.class);
        if(map == null){
            return new HashMap<>();
        }
        return map;
    }

    /**
     * Convert any object to JSON string
     * @param obj
     * @return
     */
    public static String toJSONString(Object obj) {
        if(obj == null){
            return "";
        }
        try {
            if(obj instanceof String){
                return obj.toString();
            } else {
                return JSON.toJSONString(obj);
            }
        } catch (Exception e){
            logger.error("Convert object to JSON string exception", e);
            return "";
        }
    }
}
