package org.linghu.message.client;

import org.linghu.message.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.linghu.message.dto.UserInfo;

import java.util.Set;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "linghuzhiyan-user-service",path="/api/internal/users")
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{id}")
    Result<UserInfo> getUserById(@PathVariable("id") String id);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/username/{username}")
    Result<UserInfo> getUserByUsername(@PathVariable("username") String username);

    /**
     * 根据用户ID获取用户角色ID集合
     */
    @GetMapping("/{userId}/roles")
    Result<Set<String>> getUserRoleIds(@PathVariable("userId") String userId);

}
