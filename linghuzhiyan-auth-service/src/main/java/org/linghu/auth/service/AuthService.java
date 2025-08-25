package org.linghu.auth.service;

import org.linghu.auth.dto.LoginRequestDTO;
import org.linghu.auth.dto.LoginResponseDTO;
import org.linghu.auth.dto.Result;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    Result<LoginResponseDTO> login(LoginRequestDTO loginRequest);

    /**
     * 用户登出
     * @param token JWT令牌
     * @return 操作结果
     */
    Result<Void> logout(String token);

    /**
     * 刷新令牌
     * @param refreshToken 刷新令牌
     * @return 新的JWT令牌
     */
    Result<LoginResponseDTO> refreshToken(String refreshToken);

    /**
     * 验证令牌
     * @param token JWT令牌
     * @return 验证结果
     */
    Result<Boolean> validateToken(String token);

    /**
     * 从令牌中获取用户名
     * @param token JWT令牌
     * @return 用户名
     */
    Result<String> getUsernameFromToken(String token);
}
