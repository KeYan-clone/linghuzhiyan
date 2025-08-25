package org.linghu.experiment.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON工具类，提供JSON相关的操作方法
 */
public class JsonUtils {

    private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将对象转换为JSON字符串
     * 
     * @param object 要转换的对象
     * @return JSON字符串，如果转换失败则返回null
     */
    public static String toJsonString(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.error("Convert object to JSON string error: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON字符串解析为对象
     * 
     * @param json      JSON字符串
     * @param valueType 目标类型
     * @return 解析后的对象，如果解析失败则返回null
     */
    public static <T> T parseObject(String json, Class<T> valueType) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, valueType);
        } catch (JsonProcessingException e) {
            logger.error("Parse JSON string error: {}", e.getMessage());
            return null;
        }
    }    /**
     * 将用户资料转换为JSON字符串
     * 
     * @param profile 用户资料对象
     * @return JSON字符串
     */
    public static String standardizeProfile(Object profile) {
        if (profile == null) {
            return "{}";
        }

        try {
            return objectMapper.writeValueAsString(profile);
        } catch (JsonProcessingException e) {
            logger.error("转换用户资料为JSON字符串时出错: {}", e.getMessage());
            return "{}";
        }
    }
    
    /**
     * 将用户资料字符串转换为JSON字符串
     * 
     * @param profileStr 用户资料字符串
     * @return JSON字符串
     */
    public static String standardizeProfile(String profileStr) {
        if (StringUtils.isBlank(profileStr)) {
            return "{}";
        }
        
        try {
            // 尝试解析JSON
            objectMapper.readTree(profileStr);
            return profileStr; // 如果是有效JSON，直接返回
        } catch (Exception e) {
            // 如果不是有效JSON，包装为info字段
            ObjectNode rootNode = objectMapper.createObjectNode();
            rootNode.put("info", profileStr);
            try {
                return objectMapper.writeValueAsString(rootNode);
            } catch (Exception ex) {
                logger.error("包装用户资料为JSON时出错: {}", ex.getMessage());
                return "{}";
            }
        }
    }

    /**
     * 获取用户资料中的特定字段值
     * 
     * @param profileStr 用户资料JSON字符串
     * @param fieldName  字段名
     * @return 字段值，如不存在或出错则返回空字符串
     */
    public static String getProfileField(String profileStr, String fieldName) {
        if (StringUtils.isBlank(profileStr)) {
            return "";
        }

        try {
            JsonNode rootNode = objectMapper.readTree(profileStr);
            JsonNode fieldNode = rootNode.get(fieldName);
            return fieldNode != null ? fieldNode.asText("") : "";
        } catch (Exception e) {
            logger.warn("获取用户资料字段时出错: {}", e.getMessage());
            return "";
        }
    }    /**
     * 更新用户资料中的特定字段
     * 
     * @param profileStr 原用户资料JSON字符串
     * @param fieldName  要更新的字段名
     * @param fieldValue 新的字段值
     * @return 更新后的用户资料JSON字符串
     */
    public static String updateProfileField(String profileStr, String fieldName, String fieldValue) {
        if (StringUtils.isBlank(profileStr) || profileStr.equals("{}")) {
            // 如果是空JSON，创建一个新的
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put(fieldName, fieldValue);
            try {
                return objectMapper.writeValueAsString(objectNode);
            } catch (Exception e) {
                logger.error("更新用户资料字段时出错: {}", e.getMessage());
                return "{}";
            }
        }

        try {
            JsonNode rootNode = objectMapper.readTree(profileStr);
            ObjectNode objectNode = (ObjectNode) rootNode;
            objectNode.put(fieldName, fieldValue);
            return objectMapper.writeValueAsString(objectNode);
        } catch (Exception e) {
            logger.error("更新用户资料字段时出错: {}", e.getMessage());
            
            // 如果解析失败，创建新的JSON对象
            ObjectNode objectNode = objectMapper.createObjectNode();
            objectNode.put(fieldName, fieldValue);
            try {
                return objectMapper.writeValueAsString(objectNode);
            } catch (Exception ex) {
                logger.error("创建用户资料JSON时出错: {}", ex.getMessage());
                return "{}";
            }
        }
    }
}