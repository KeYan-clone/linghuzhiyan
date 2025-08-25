package org.linghu.auth.service;

/**
 * 登录日志服务接口
 */
public interface LoginLogService {

    /**
     * 记录成功登录日志
     * @param userId 用户ID
     * @param ipAddress IP地址
     * @param deviceType 设备类型
     * @param loginInfo 登录信息
     */
    void logSuccessfulLogin(String userId, String ipAddress, String deviceType, String loginInfo);

    /**
     * 记录失败登录日志
     * @param username 用户名
     * @param ipAddress IP地址
     * @param deviceType 设备类型
     * @param reason 失败原因
     * @param loginInfo 登录信息
     */
    void logFailedLogin(String username, String ipAddress, String deviceType, String reason, String loginInfo);
}
