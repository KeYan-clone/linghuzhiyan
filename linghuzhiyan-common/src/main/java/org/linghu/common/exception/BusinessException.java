package org.linghu.common.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 
 * @author linghu
 * @version 1.0.0
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final Integer code;
    
    /**
     * 错误消息
     */
    private final String message;
    
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
    
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
    
    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }
    
    /**
     * 创建参数错误异常
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(400, message);
    }
    
    /**
     * 创建未授权异常
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(401, message);
    }
    
    /**
     * 创建禁止访问异常
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(403, message);
    }
    
    /**
     * 创建资源未找到异常
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(404, message);
    }
}
