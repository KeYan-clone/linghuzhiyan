package org.linghu.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON工具类
 * 
 * @author linghu
 * @version 1.0.0
 */
@Slf4j
public class JsonUtils {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    static {
        // 注册时间模块
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }
    
    /**
     * 对象转JSON字符串
     * 
     * @param obj 对象
     * @return JSON字符串
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("对象转JSON失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * JSON字符串转对象
     * 
     * @param json JSON字符串
     * @param clazz 目标类型
     * @return 对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("JSON转对象失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 格式化JSON字符串
     * 
     * @param json JSON字符串
     * @return 格式化后的JSON字符串
     */
    public static String prettyFormat(String json) {
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON格式化失败: {}", e.getMessage());
            return json;
        }
    }
}
