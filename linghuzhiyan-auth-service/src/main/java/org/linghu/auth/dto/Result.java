package org.linghu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应结果包装类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {
    
    private int code;
    private String message;
    private T data;
    
    public static <T> Result<T> success() {
        return new Result<>(200, "操作成功", null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(200, message, data);
    }
    
    public static <T> Result<T> success(String message) {
        return new Result<>(200, message, null);
    }
    
    public static <T> Result<T> failure(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    public static <T> Result<T> failure(String message) {
        return new Result<>(400, message, null);
    }
    
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 判断操作是否成功
     * @return true if success, false otherwise
     */
    public boolean isSuccess() {
        return this.code == 200;
    }
    
    /**
     * 判断操作是否失败
     * @return true if failed, false otherwise
     */
    public boolean isFailure() {
        return !isSuccess();
    }
}
