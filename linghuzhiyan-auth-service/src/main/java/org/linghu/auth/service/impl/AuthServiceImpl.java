package org.linghu.auth.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.linghu.auth.client.UserServiceClient;
import org.linghu.auth.dto.*;
import org.linghu.auth.repository.UserRoleRelationRepository;
import org.linghu.auth.security.JwtTokenUtil;
import org.linghu.auth.service.AuthService;
import org.linghu.auth.service.LoginLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 认证服务实现类
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserServiceClient userServiceClient;
    private final JwtTokenUtil jwtTokenUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogService loginLogService;
    private final UserRoleRelationRepository userRoleRelationRepository;

    @Autowired
    public AuthServiceImpl(UserServiceClient userServiceClient,
                          JwtTokenUtil jwtTokenUtil,
                          PasswordEncoder passwordEncoder,
                          LoginLogService loginLogService,
                          UserRoleRelationRepository userRoleRelationRepository) {
        this.userServiceClient = userServiceClient;
        this.jwtTokenUtil = jwtTokenUtil;
        this.passwordEncoder = passwordEncoder;
        this.loginLogService = loginLogService;
        this.userRoleRelationRepository = userRoleRelationRepository;
    }

    @Override
    public Result<LoginResponseDTO> login(LoginRequestDTO loginRequest) {
        try {
            // 1. 调用用户服务验证用户名和密码
            Result<UserInfo> userResult = userServiceClient.validateUser(
                loginRequest.getUsername(), loginRequest.getPassword());
            
            if (!userResult.isSuccess()) {
                // 记录失败登录日志
                loginLogService.logFailedLogin(
                    loginRequest.getUsername(),
                    loginRequest.getIpAddress(),
                    loginRequest.getDeviceType(),
                    "用户名或密码错误",
                    loginRequest.getLoginInfo()
                );
                return Result.error(401,"用户名或密码错误");
            }

            UserInfo user = userResult.getData();
            if (user == null) {
                loginLogService.logFailedLogin(
                    loginRequest.getUsername(),
                    loginRequest.getIpAddress(),
                    loginRequest.getDeviceType(),
                    "用户信息为空",
                    loginRequest.getLoginInfo()
                );
                return Result.error(401, "用户信息获取失败");
            }
            
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                loginLogService.logFailedLogin(
                    loginRequest.getUsername(),
                    loginRequest.getIpAddress(),
                    loginRequest.getDeviceType(),
                    "用户名为空",
                    loginRequest.getLoginInfo()
                );
                return Result.error(401, "用户名信息无效");
            }
            
            if (user.getIsDeleted() != null && user.getIsDeleted()) {
                loginLogService.logFailedLogin(
                    loginRequest.getUsername(),
                    loginRequest.getIpAddress(),
                    loginRequest.getDeviceType(),
                    "用户已被删除",
                    loginRequest.getLoginInfo()
                );
                return Result.error(410,"用户已被删除");
            }

            // 2. 获取用户角色
            Set<String> roleIds = userRoleRelationRepository.findRoleIdsByUserId(user.getId());
            // 如果用户没有角色，给一个默认的学生角色
            if (roleIds.isEmpty()) {
                roleIds = new HashSet<>();
                roleIds.add("ROLE_STUDENT");
            }

            // 3. 验证请求的角色是否有效
            String requestedRole = loginRequest.getRole();
            if (requestedRole != null && !requestedRole.trim().isEmpty()) {
                // 标准化角色名称（确保以ROLE_开头）
                String normalizedRequestedRole = requestedRole.startsWith("ROLE_") ? 
                    requestedRole : "ROLE_" + requestedRole;
                
                // 检查用户是否拥有请求的角色
                if (!roleIds.contains(normalizedRequestedRole)) {
                    loginLogService.logFailedLogin(
                        loginRequest.getUsername(),
                        loginRequest.getIpAddress(),
                        loginRequest.getDeviceType(),
                        "用户没有请求的角色权限: " + requestedRole,
                        loginRequest.getLoginInfo()
                    );
                    return Result.error(403, "您没有请求的角色权限: " + requestedRole);
                }
                
                // 如果指定了角色，只使用该角色进行登录
                roleIds = Set.of(normalizedRequestedRole);
                log.info("用户 {} 使用指定角色 {} 登录", user.getUsername(), normalizedRequestedRole);
            } else {
                log.info("用户 {} 使用所有角色登录: {}", user.getUsername(), roleIds);
            }

            // 4. 创建UserDetails对象
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            for (String roleId : roleIds) {
                if (roleId != null && !roleId.trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(roleId));
                }
            }

            UserDetails userDetails = User.builder()
                    .username(user.getUsername())
                    .password("") // 密码不需要在令牌中
                    .authorities(authorities)
                    .build();
            
            // 确保 userDetails 不为空
            if (userDetails == null || userDetails.getUsername() == null) {
                loginLogService.logFailedLogin(
                    loginRequest.getUsername(),
                    loginRequest.getIpAddress(),
                    loginRequest.getDeviceType(),
                    "UserDetails 创建失败",
                    loginRequest.getLoginInfo()
                );
                return Result.error(500, "用户认证信息创建失败");
            }

            // 5. 生成JWT令牌
            String accessToken = jwtTokenUtil.generateToken(userDetails);
            String refreshToken = generateRefreshToken(user.getUsername());

            // 6. 记录成功登录日志
            loginLogService.logSuccessfulLogin(
                user.getId(),
                loginRequest.getIpAddress(),
                loginRequest.getDeviceType(),
                loginRequest.getLoginInfo()
            );

            // 7. 构建响应
            LoginResponseDTO response = new LoginResponseDTO();
            response.setAccessToken(accessToken);
            response.setToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(jwtTokenUtil.getExpirationDateFromToken(accessToken).getTime());
            response.setUser(user);

            return Result.success("登录成功", response);

        } catch (Exception e) {
            log.error("登录过程中发生异常", e);
            return Result.error("登录失败，请稍后重试");
        }
    }

    @Override
    public Result<Void> logout(String token) {
        try {
            // 1. 获取用户名（用于日志记录）
            String username = jwtTokenUtil.getUsernameFromToken(token);
            
            log.info("用户 {} 退出登录", username);
            return Result.success("退出登录成功");

        } catch (Exception e) {
            log.error("退出登录过程中发生异常", e);
            return Result.error("退出登录失败");
        }
    }

    @Override
    public Result<LoginResponseDTO> refreshToken(String refreshToken) {
        try {
            // 实现刷新令牌逻辑
            // 这里简化实现，实际应该验证刷新令牌的有效性
            return Result.error("刷新令牌功能暂未实现");
        } catch (Exception e) {
            log.error("刷新令牌过程中发生异常", e);
            return Result.error("刷新令牌失败");
        }
    }

    @Override
    public Result<Boolean> validateToken(String token) {
        try {
            // 验证令牌
            boolean isValid = jwtTokenUtil.validateToken(token);
            return Result.success("令牌验证完成", isValid);

        } catch (Exception e) {
            log.error("验证令牌过程中发生异常", e);
            return Result.success("令牌无效", false);
        }
    }

    @Override
    public Result<String> getUsernameFromToken(String token) {
        try {
            String username = jwtTokenUtil.getUsernameFromToken(token);
            return Result.success("获取用户名成功", username);
        } catch (Exception e) {
            log.error("从令牌获取用户名过程中发生异常", e);
            return Result.error("无法从令牌获取用户名");
        }
    }

    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken(String username) {
        // 简单实现
        return "refresh_" + username + "_" + System.currentTimeMillis();
    }
}
