package org.linghu.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.linghu.auth.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * JWT认证过滤器，用于验证请求中的JWT令牌
 */
@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    
    private final JwtTokenUtil jwtTokenUtil;

    // JWT 令牌请求头
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    // JWT 令牌前缀
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    public JwtRequestFilter(JwtTokenUtil jwtTokenUtil) {
        this.jwtTokenUtil = jwtTokenUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader(this.tokenHeader);

        String username = null;
        String jwt = null;
        String ipAddress = RequestUtils.getClientIpAddress(request);
        String deviceType = RequestUtils.getDeviceType(request);

        // 从请求头中提取JWT令牌
        if (authorizationHeader != null && authorizationHeader.startsWith(this.tokenHead)) {
            jwt = authorizationHeader.substring(this.tokenHead.length());
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwt);
            } catch (Exception e) {
                log.warn("无法解析JWT令牌: {}", e.getMessage());
            }
        }

        // 如果找到用户名且当前上下文中没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // 验证令牌是否有效
                if (jwtTokenUtil.validateToken(jwt)) {
                    // 创建简单的权限集合，实际应该从用户服务获取
                    Collection<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("USER"));
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // 更新SecurityContext中的认证信息
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("已认证用户 {}, 设置安全上下文", username);

                } else {
                    log.warn("JWT令牌无效");
                }
            } catch (Exception e) {
                log.warn("认证过程中发生异常: {}", e.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}
