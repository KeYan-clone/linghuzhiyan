package org.linghu.auth.client;

import org.linghu.auth.dto.Result;
import org.linghu.auth.dto.UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "linghuzhiyan-user-service", path = "/api/internal/users")
public interface UserServiceClient {

    /**
     * 根据用户名获取用户基本信息
     */
    @GetMapping("/username/{username}")
    Result<UserInfo> getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据用户ID获取用户角色ID集合
     */
    @GetMapping("/{userId}/roles")
    Result<Set<String>> getUserRoleIds(@PathVariable("userId") String userId);

    /**
     * 验证用户名和密码
     */
    @PostMapping("/validate")
    Result<UserInfo> validateUser(@RequestParam("username") String username,
                                  @RequestParam("password") String password);
}
