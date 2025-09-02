package org.linghu.discussion.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * JWT Token 验证工具类
 * 注意：JWT生成由认证服务完成，此类只负责验证
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret:linghuzhiyan-universal-jwt-secret-key-2025-shared-across-all-microservices}")
    private String jwtSecret;

    @Value("${jwt.tokenHeader:Authorization}")
    private String tokenHeader;

    @Value("${jwt.tokenHead:Bearer}")
    private String tokenHead;

    /**
     * 获取签名密钥 - 与认证服务保持一致
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * 验证Token是否有效
     */
    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("JWT验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从Token获取认证信息
     */
    public Authentication getAuthentication(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            String rolesString = claims.get("roles", String.class);
            
            Collection<SimpleGrantedAuthority>  authorities = Arrays.stream(rolesString.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty()) // 避免空字符串
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            // 创建 UserDetails 对象作为 principal
            UserDetails userDetails = User.builder()
                    .username(username)
                    .password("") // JWT认证不需要密码
                    .authorities(authorities)
                    .build();

            return new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        } catch (Exception e) {
            log.error("从Token获取认证信息失败: {}", e.getMessage());
            return null;
        }
    }

    // Getter 方法用于配置访问
    public String getTokenHeader() {
        return tokenHeader;
    }

    public String getTokenHead() {
        return tokenHead;
    }
}
