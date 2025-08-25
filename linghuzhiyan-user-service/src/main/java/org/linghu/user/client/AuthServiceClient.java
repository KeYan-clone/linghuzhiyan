package org.linghu.user.client;

import org.linghu.user.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

/**
 * 认证服务客户端
 */
@FeignClient(name = "linghuzhiyan-auth-service", path = "/api/auth")
public interface AuthServiceClient {

    /**
     * 获取用户角色ID列表
     * @param userId 用户ID
     * @return 角色ID集合
     */
    @GetMapping("/{userId}/roles")
    Result<Set<String>> getUserRoleIds(@PathVariable("userId") String userId);

    /**
     * 设置用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/roles/{roleId}")
    Result<Void> setUserRole(@PathVariable("userId") String userId, 
                            @PathVariable("roleId") String roleId);

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    Result<Void> removeUserRole(@PathVariable("userId") String userId, 
                               @PathVariable("roleId") String roleId);
}
