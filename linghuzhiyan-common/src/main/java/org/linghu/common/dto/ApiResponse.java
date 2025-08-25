package org.linghu.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一API响应格式
 * 
 * @author linghu
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    /**
     * 私有构造器，用于创建带时间戳的响应
     */
    private ApiResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 成功响应
     * 
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data);
    }
    
    /**
     * 成功响应（无数据）
     * 
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应（自定义消息）
     * 
     * @param message 响应消息
     * @param data 响应数据
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }
    
    /**
     * 失败响应
     * 
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null);
    }
    
    /**
     * 失败响应（自定义状态码）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
    
    /**
     * 参数错误响应
     * 
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, null);
    }
    
    /**
     * 未授权响应
     * 
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, null);
    }
    
    /**
     * 禁止访问响应
     * 
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, null);
    }
    
    /**
     * 资源未找到响应
     * 
     * @param message 错误消息
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, null);
    }
    
    /**
     * 判断是否成功
     * 
     * @return true if success
     */
    public boolean isSuccess() {
        return this.code != null && this.code == 200;
    }
}
