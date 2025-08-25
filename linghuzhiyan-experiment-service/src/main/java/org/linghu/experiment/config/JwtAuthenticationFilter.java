package org.linghu.experiment.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT认证过滤器
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final String jwtSecret;
    private final String tokenHeader;
    private final String tokenHead;

    public JwtAuthenticationFilter(String jwtSecret, String tokenHeader, String tokenHead) {
        this.jwtSecret = jwtSecret;
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
                // 解析JWT令牌
                SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(authToken)
                        .getBody();

                String username = claims.getSubject();
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 创建权限列表
                    List<GrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    // 创建用户详情
                    UserDetails userDetails = User.builder()
                            .username(username)
                            .password("")
                            .authorities(authorities)
                            .build();

                    // 设置认证信息
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("JWT认证成功: 用户={}, 角色={}", username, roles);
                }
            } catch (Exception e) {
                log.warn("JWT令牌解析失败: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
