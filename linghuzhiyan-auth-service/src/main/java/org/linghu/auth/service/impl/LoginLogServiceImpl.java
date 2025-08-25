package org.linghu.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.linghu.auth.domain.LoginLog;
import org.linghu.auth.repository.LoginLogRepository;
import org.linghu.auth.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 登录日志服务实现类
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {

    private final LoginLogRepository loginLogRepository;

    @Autowired
    public LoginLogServiceImpl(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }

    @Override
    public void logSuccessfulLogin(String userId, String ipAddress, String deviceType, String loginInfo) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setIpAddress(ipAddress);
            loginLog.setDeviceType(deviceType);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus("SUCCESS");
            loginLog.setFailureReason(null);
            loginLog.setLoginInfo(loginInfo);

            loginLogRepository.save(loginLog);
            log.info("记录用户 {} 成功登录日志", userId);
        } catch (Exception e) {
            log.error("记录成功登录日志失败", e);
        }
    }

    @Override
    public void logFailedLogin(String username, String ipAddress, String deviceType, String reason, String loginInfo) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(null); // 失败登录时可能没有用户ID
            loginLog.setIpAddress(ipAddress);
            loginLog.setDeviceType(deviceType);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setStatus("FAILED");
            loginLog.setFailureReason(reason);
            loginLog.setLoginInfo(loginInfo);

            loginLogRepository.save(loginLog);
            log.warn("记录用户 {} 失败登录日志: {}", username, reason);
        } catch (Exception e) {
            log.error("记录失败登录日志失败", e);
        }
    }
}
