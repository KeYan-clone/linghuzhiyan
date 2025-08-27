package org.linghu.resource.exception;

/**
 * 资源业务异常
 */
public class ResourceException extends RuntimeException {
    
    public ResourceException(String message) {
        super(message);
    }
    
    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
