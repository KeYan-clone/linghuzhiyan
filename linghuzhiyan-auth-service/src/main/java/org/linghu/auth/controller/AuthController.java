package org.linghu.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.linghu.auth.dto.LoginRequestDTO;
import org.linghu.auth.dto.LoginResponseDTO;
import org.linghu.auth.dto.Result;
import org.linghu.auth.service.AuthService;
import org.linghu.auth.utils.RequestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证服务", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户使用用户名和密码登录系统")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                         HttpServletRequest request) {
        // 补充请求信息
        loginRequest.setIpAddress(RequestUtils.getClientIpAddress(request));
        loginRequest.setDeviceType(RequestUtils.getDeviceType(request));
        loginRequest.setLoginInfo(RequestUtils.collectRequestInfo(request));

        log.info("用户 {} 尝试登录，IP: {}, 设备: {}", 
                loginRequest.getUsername(), 
                loginRequest.getIpAddress(), 
                loginRequest.getDeviceType());

        return authService.login(loginRequest);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录系统")
    public Result<Void> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        if (authHeader == null || !authHeader.startsWith(tokenHead)) {
            return Result.error("请求头中缺少有效的令牌");
        }

        String token = authHeader.substring(tokenHead.length());
        return authService.logout(token);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<LoginResponseDTO> refreshToken(
            @Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @GetMapping("/validate")
    @Operation(summary = "验证令牌", description = "验证JWT令牌的有效性")
    public Result<Boolean> validateToken(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        if (authHeader == null || !authHeader.startsWith(tokenHead)) {
            return Result.success("令牌格式错误", false);
        }

        String token = authHeader.substring(tokenHead.length());
        return authService.validateToken(token);
    }

    @GetMapping("/user")
    @Operation(summary = "获取当前用户", description = "从令牌中获取当前登录用户信息")
    public Result<String> getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader(tokenHeader);
        if (authHeader == null || !authHeader.startsWith(tokenHead)) {
            return Result.error("请求头中缺少有效的令牌");
        }

        String token = authHeader.substring(tokenHead.length());
        return authService.getUsernameFromToken(token);
    }
}
