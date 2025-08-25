package org.linghu.user.exception;

/**
 * 用户业务异常
 */
public class UserException extends RuntimeException {
    
    public UserException(String message) {
        super(message);
    }
    
    public UserException(String message, Throwable cause) {
        super(message, cause);
    }
    
    // 静态工厂方法
    public static UserException userNotFound() {
        return new UserException("用户不存在");
    }
    
    public static UserException usernameAlreadyExists() {
        return new UserException("用户名已存在");
    }
    
    public static UserException emailAlreadyExists() {
        return new UserException("邮箱已存在");
    }
    
    public static UserException userDeleted() {
        return new UserException("用户已被删除");
    }
    
    public static UserException invalidCredentials() {
        return new UserException("用户名或密码错误");
    }
    
    public static UserException roleNotAuthorized() {
        return new UserException("用户没有指定角色的权限");
    }
    
    public static UserException insufficientPermissions() {
        return new UserException("权限不足");
    }
}
