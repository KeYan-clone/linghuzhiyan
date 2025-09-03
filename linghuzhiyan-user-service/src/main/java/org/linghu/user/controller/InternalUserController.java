package org.linghu.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.user.domain.User;
import org.linghu.user.dto.Result;
import org.linghu.user.dto.UserDTO;
import org.linghu.user.dto.UserInfo;
import org.linghu.user.repository.UserRepository;
import org.linghu.user.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 用户服务内部API控制器
 * 供其他微服务内部调用，无需用户认证
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
@Tag(name = "用户服务内部API", description = "供微服务间调用的内部接口")
public class InternalUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 根据用户名获取用户基本信息
     * @param username 用户名
     * @return 用户基本信息
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户基本信息", description = "供服务间调用的获取用户信息接口")
    public Result<UserInfo> getUserByUsername(@PathVariable("username") String username) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("内部API调用 - 用户不存在: username={}", username);
                return Result.error("用户不存在: " + username);
            }

            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                log.warn("内部API调用 - 用户已被删除: username={}", username);
                return Result.error("用户已被删除: " + username);
            }

            // 获取用户角色
            Set<String> roles = userService.getUserRoleIds(user.getId());

            UserInfo UserInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsDeleted(),
                roles,
                user.getCreatedAt()
            );

            log.info("内部API调用 - 获取用户信息成功: username={}, userId={}", username, user.getId());
            return Result.success(UserInfo);
        } catch (Exception e) {
            log.error("内部API调用 - 获取用户信息失败: username={}", username, e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据用户ID获取用户基本信息", description = "供服务间调用的获取用户信息接口")
    public Result<UserInfo> getUserById(@PathVariable("id") String id) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                log.warn("内部API调用 - 用户不存在: userId={}", id);
                return Result.error("用户不存在: " + id);
            }

            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                log.warn("内部API调用 - 用户已被删除: userId={}", id);
                return Result.error("用户已被删除: " + id);
            }

            // 获取用户角色
            Set<String> roles = userService.getUserRoleIds(user.getId());

            UserInfo UserInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsDeleted(),
                roles,
                user.getCreatedAt()
            );

            log.info("内部API调用 - 获取用户信息成功: userId={}", id);
            return Result.success(UserInfo);
        } catch (Exception e) {
            log.error("内部API调用 - 获取用户信息失败: userId={}", id, e);
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 根据用户ID获取用户角色ID集合
     * @param userId 用户ID
     * @return 角色ID集合
     */
    @GetMapping("/{userId}/roles")
    @Operation(summary = "根据用户ID获取角色", description = "供服务间调用的获取用户角色接口")
    public Result<Set<String>> getUserRoleIds(@PathVariable("userId") String userId) {
        try {
            Set<String> roleIds = userService.getUserRoleIds(userId);
            log.info("内部API调用 - 获取用户角色成功: userId={}, roles={}", userId, roleIds);
            return Result.success(roleIds);
        } catch (Exception e) {
            log.error("内部API调用 - 获取用户角色失败: userId={}", userId, e);
            return Result.error("获取用户角色失败: " + e.getMessage());
        }
    }

    /**
     * 验证用户名和密码
     * @param username 用户名
     * @param password 密码
     * @return 验证结果和用户基本信息
     */
    @PostMapping("/validate")
    @Operation(summary = "验证用户名和密码", description = "供服务间调用的用户验证接口")
    public Result<UserInfo> validateUser(@RequestParam("username") String username, 
                                            @RequestParam("password") String password) {
        try {
            Optional<User> userOpt = userService.findByUsername(username);
            if (userOpt.isEmpty()) {
                log.warn("内部API调用 - 用户验证失败，用户不存在: username={}", username);
                return Result.error("用户名或密码错误");
            }

            User user = userOpt.get();
            if (Boolean.TRUE.equals(user.getIsDeleted())) {
                log.warn("内部API调用 - 用户验证失败，用户已被删除: username={}", username);
                return Result.error("用户已被删除");
            }

            // 验证密码
            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("内部API调用 - 用户验证失败，密码错误: username={}", username);
                return Result.error("用户名或密码错误");
            }

            // 获取用户角色
            Set<String> roles = userService.getUserRoleIds(user.getId());

            UserInfo UserInfo = new UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getIsDeleted(),
                roles,
                user.getCreatedAt()
            );

            log.info("内部API调用 - 用户验证成功: username={}, userId={}", username, user.getId());
            return Result.success(UserInfo);
        } catch (Exception e) {
            log.error("内部API调用 - 用户验证失败: username={}", username, e);
            return Result.error("用户验证失败: " + e.getMessage());
        }
    }

    /**
     * 实验中根据用户Id信息
     */
    @GetMapping("/exp/{id}")
    @Operation(summary = "实验中根据用户Id信息", description = "供实验服务间调用的获取用户信息接口")
    public UserDTO getUserByIdInExp(@PathVariable("id") String id) {
        return userService.getUserById(id);
    }

    /**
     * 实验中根据用户名获取用户信息
     */
    @GetMapping("/exp/username/{username}")
    @Operation(summary = "实验中根据用户名获取用户信息", description = "供实验服务间调用的获取用户信息接口")
    public UserDTO getUserByUsernameInExp(@PathVariable("username") String username) {
         return userService.getUserByUsername(username);
    }

    /**
     * 实验中批量获取用户信息
     */
    @PostMapping("/exp/batch")
    @Operation(summary = "实验中批量获取用户信息", description = "供实验服务间调用的批量获取用户信息接口")
    public List<UserDTO> getUsersByIdsInExp(@RequestBody List<String> userIds) {
        return userService.getUsersByIds(userIds);
    }

    /**
     * 实验中获取所有用户信息
     */
    @GetMapping("/exp/all")
    @Operation(summary = "实验中获取所有用户信息", description = "供实验服务间调用的获取所有用户信息接口")
    public List<UserDTO> getAllUsersInExp() {
        return userService.getAllUsers();
    }
    /**
     * 健康检查接口
     * @return 服务状态
     */
    @GetMapping("/health")
    @Operation(summary = "内部健康检查", description = "检查内部API服务状态")
    public Result<String> health() {
        log.debug("用户服务内部API健康检查");
        return Result.success("User Internal API is healthy");
    }
}
