package org.linghu.discussion.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final String tokenHeader;
    private final String tokenHead;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, String tokenHeader, String tokenHead) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenHeader = tokenHeader;
        this.tokenHead = tokenHead;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader(tokenHeader);
        
        if (authHeader != null && authHeader.startsWith(tokenHead)) {
            String authToken = authHeader.substring(tokenHead.length());
            
            try {
                // 使用JwtTokenProvider验证和解析token
                if (jwtTokenProvider.validateToken(authToken)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(authToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    String username = jwtTokenProvider.getUsernameFromToken(authToken);
                    log.debug("JWT认证成功: 用户={}", username);
                }
            } catch (Exception e) {
                log.warn("JWT令牌解析失败: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
