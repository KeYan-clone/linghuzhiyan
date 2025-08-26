package org.linghu.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.auth.domain.UserRoleId;
import org.linghu.auth.domain.UserRoleRelation;
import org.linghu.auth.dto.Result;
import org.linghu.auth.repository.RoleRepository;
import org.linghu.auth.repository.UserRoleRelationRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 认证服务内部API控制器
 * 供其他微服务内部调用，无需用户认证
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/auth")
@RequiredArgsConstructor
@Tag(name = "认证服务内部API", description = "供微服务间调用的内部接口")
public class InternalAuthController {

    private final UserRoleRelationRepository userRoleRelationRepository;
    private final RoleRepository roleRepository;

    /**
     * 获取用户角色ID集合
     * @param userId 用户ID
     * @return 角色ID集合
     */
    @GetMapping("/{userId}/roles")
    @Operation(summary = "获取用户角色", description = "供服务间调用的获取用户角色接口")
    public Result<Set<String>> getUserRoleIds(@PathVariable("userId") String userId) {
        try {
            // 从数据库获取用户的角色ID集合
            Set<String> roleIds = userRoleRelationRepository.findRoleIdsByUserId(userId);
            
            // 如果用户没有角色，给一个默认的学生角色
            if (roleIds.isEmpty()) {
                roleIds = new HashSet<>();
                roleIds.add("ROLE_STUDENT");
            }
            
            log.info("内部API调用 - 获取用户 {} 的角色: {}", userId, roleIds);
            return Result.success(roleIds);
        } catch (Exception e) {
            log.error("获取用户角色失败: userId={}", userId, e);
            return Result.error("获取用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 设置用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @PostMapping("/{userId}/roles/{roleId}")
    @Transactional
    @Operation(summary = "设置用户角色", description = "供服务间调用的设置用户角色接口")
    public Result<Void> setUserRole(@PathVariable("userId") String userId, 
                                   @PathVariable("roleId") String roleId) {
        try {
            // 检查角色是否存在
            if (!roleRepository.existsById(roleId)) {
                log.warn("角色不存在: roleId={}", roleId);
                return Result.error("角色不存在: " + roleId);
            }
            
            // 检查用户是否已经具有该角色
            if (userRoleRelationRepository.existsByIdUserIdAndIdRoleId(userId, roleId)) {
                log.info("用户 {} 已经具有角色 {}", userId, roleId);
                return Result.success();
            }
            
            // 创建用户角色关系
            UserRoleId userRoleId = new UserRoleId(userId, roleId);
            UserRoleRelation userRoleRelation = new UserRoleRelation(userRoleId, null);
            userRoleRelationRepository.save(userRoleRelation);
            
            log.info("内部API调用 - 为用户 {} 设置角色 {} 成功", userId, roleId);
            return Result.success();
        } catch (Exception e) {
            log.error("设置用户角色失败: userId={}, roleId={}", userId, roleId, e);
            return Result.error("设置用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 移除用户角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @Transactional
    @Operation(summary = "移除用户角色", description = "供服务间调用的移除用户角色接口")
    public Result<Void> removeUserRole(@PathVariable("userId") String userId, 
                                      @PathVariable("roleId") String roleId) {
        try {
            // 检查用户是否具有该角色
            if (!userRoleRelationRepository.existsByIdUserIdAndIdRoleId(userId, roleId)) {
                log.info("用户 {} 没有角色 {}，无需移除", userId, roleId);
                return Result.success();
            }
            
            // 删除用户角色关系
            UserRoleId userRoleId = new UserRoleId(userId, roleId);
            userRoleRelationRepository.deleteById(userRoleId);
            
            log.info("内部API调用 - 为用户 {} 移除角色 {} 成功", userId, roleId);
            return Result.success();
        } catch (Exception e) {
            log.error("移除用户角色失败: userId={}, roleId={}", userId, roleId, e);
            return Result.error("移除用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 健康检查接口
     * @return 服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "内部健康检查", description = "检查内部API服务状态")
    public Result<String> health() {
        log.debug("内部API健康检查");
        return Result.success("Internal API is healthy");
    }
}
