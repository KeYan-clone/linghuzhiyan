package org.linghu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {
    
    /**
     * 用户基本信息
     */
    private UserBasicDTO user;
    
    /**
     * 访问令牌
     */
    private String accessToken;
    
    /**
     * 刷新令牌
     */
    private String refreshToken;
    
    /**
     * JWT token（兼容字段）
     */
    private String token;
    
    /**
     * token类型
     */
    private String tokenType;
    
    /**
     * token过期时间（毫秒时间戳）
     */
    private long expiresIn;
}
