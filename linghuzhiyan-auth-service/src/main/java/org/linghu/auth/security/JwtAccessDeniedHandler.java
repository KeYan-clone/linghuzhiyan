package org.linghu.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT访问被拒处理器 - 处理已认证但权限不足的情况（403）
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        // 1. 设置编码和内容类型
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        
        // 2. 准备错误响应
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("status", 403);
        errorData.put("message", "权限不足，无法访问");
        errorData.put("path", request.getRequestURI());
        errorData.put("error", accessDeniedException.getMessage());
        
        // 3. 记录权限不足的日志
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = "未知用户";
            if (authentication != null && authentication.getName() != null) {
                username = authentication.getName();
            }
            
            log.warn("用户 {} 尝试访问权限不足的资源: {}", username, request.getRequestURI());
            
        } catch (Exception e) {
            log.error("记录权限不足日志时发生异常", e);
        }
        
        // 4. 使用ObjectMapper将错误信息转换为JSON并写入响应
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), errorData);
    }
}
