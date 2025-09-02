package org.linghu.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * JWT认证过滤器
 * 
 * @author linghu
 * @version 1.0.0
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    private static final List<String> SKIP_AUTH_URLS = List.of(
        "/api/auth/login",
        "/api/users/register",
        "/api/auth/refresh",
        "/actuator/health"
    );

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();
            
            // 跳过不需要认证的URL
            if (SKIP_AUTH_URLS.stream().anyMatch(path::startsWith)) {
                return chain.filter(exchange);
            }
            
            // 获取token
            String token = getTokenFromRequest(request);
            if (!StringUtils.hasText(token)) {
                return unauthorizedResponse(exchange, "Token不能为空");
            }
            
            // 验证token
            try {
                Claims claims = validateToken(token);
                if (claims == null) {
                    return unauthorizedResponse(exchange, "Token无效");
                }
                
                // 在请求头中添加用户信息
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", claims.getSubject())
                    .header("username", claims.get("username", String.class))
                    .build();
                
                return chain.filter(exchange.mutate().request(modifiedRequest).build());
                
            } catch (Exception e) {
                log.error("Token验证失败: {}", e.getMessage());
                return unauthorizedResponse(exchange, "Token验证失败");
            }
        };
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(tokenHeader);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(tokenHead + " ")) {
            return authHeader.substring(tokenHead.length() + 1);
        }
        return null;
    }

    private Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        String body = String.format("{\"code\": 401, \"message\": \"%s\", \"data\": null}", message);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // 配置类，可以添加配置参数
    }
}
