package org.linghu.user.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.user.client.AuthServiceClient;
import org.linghu.user.constants.SystemConstants;
import org.linghu.user.domain.User;
import org.linghu.user.dto.*;
import org.linghu.user.exception.UserException;
import org.linghu.user.repository.UserRepository;
import org.linghu.user.service.UserService;
import org.linghu.user.utils.JsonUtils;
import org.linghu.user.utils.MinioUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthServiceClient authServiceClient;
    private final MinioUtil minioUtil;

    // 默认头像
    public static final String DEFAULT_AVATAR_URL = "/default-avatar.png";


    // 头像URL过期时间(秒)，默认1小时
    @Value("${minio.avatar.url.expiry:3600}")
    private int avatarUrlExpiry;

    @Override
    @Transactional
    public UserDTO registerUser(UserRegistrationDTO registrationDTO) {
        // 检查用户名和邮箱是否已存在
        if (existsByUsername(registrationDTO.getUsername())) {
            throw UserException.usernameAlreadyExists();
        }
        if (existsByEmail(registrationDTO.getEmail())) {
            throw UserException.emailAlreadyExists();
        }

        // 创建用户实体
        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setProfile("{}"); // 设置空的用户资料
        user.setAvatar(DEFAULT_AVATAR_URL);
        user.setIsDeleted(false);

        // 保存用户
        User savedUser = userRepository.save(user);
        
        // 默认分配学生角色（通过认证服务）
        try {
            authServiceClient.setUserRole(savedUser.getId(), SystemConstants.ROLE_STUDENT);
            log.info("为新用户 {} 分配学生角色", savedUser.getUsername());
        } catch (Exception e) {
            log.warn("为新用户分配角色失败: {}", e.getMessage());
        }

        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public void deleteUser(String userId, String currentUsername) {
        checkUserNotDeleted(userId);
        
        // 验证当前用户
        User currentUser = findByUsername(currentUsername)
                .orElseThrow(UserException::userNotFound);

        // 验证要删除的用户
        User targetUser = userRepository.findById(userId)
                .orElseThrow(UserException::userNotFound);

        // 获取当前用户角色
        Set<String> currentUserRoles = getUserRoleIds(currentUser.getId());

        // 只有管理员才能进行删除操作
        if (!currentUserRoles.contains(SystemConstants.ROLE_ADMIN)) {
            throw UserException.insufficientPermissions();
        }

        // 获取目标用户角色
        Set<String> targetUserRoles = getUserRoleIds(targetUser.getId());

        // 验证权限：管理员可以删除自己以及其他低于管理员权限的账户,抛出权限不足的异常
        if (targetUserRoles.contains(SystemConstants.ROLE_ADMIN) && !userId.equals(currentUser.getId())) {
            throw UserException.insufficientPermissions();
        }

        // 执行软删除
        // targetUser.setIsDeleted(true);
        // userRepository.save(targetUser);

        userRepository.delete(targetUser);
        
        log.info("用户 {} 已被用户 {} 删除", userId, currentUsername);
    }

    @Override
    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(UserException::userNotFound);
        checkUserNotDeleted(user.getId());
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);
        checkUserNotDeleted(username);
        return convertToDTO(user);
    }

    @Override
    public List<UserDTO> getUsersByIds(List<String> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : users) {
            if (!user.getIsDeleted()) {
                userDTOs.add(convertToDTO(user));
            }
        }

        return userDTOs;
    }

    @Override
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findByIsDeletedFalse();
        List<UserDTO> userDTOs = new ArrayList<>();

        for (User user : users) {
            userDTOs.add(convertToDTO(user));
        }

        return userDTOs;
    }

    @Override
    @Transactional
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);
        
        checkUserNotDeleted(user.getId());

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw  UserException.invalidCredentials();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("用户 {} 修改密码成功", username);
    }

    @Override
    public Page<UserDTO> listUsers(int pageNum, int pageSize) {
        // 页码从0开始计算
        Pageable pageable = PageRequest.of(pageNum - 1, pageSize);
        // 只查询未被软删除的用户
        Page<User> userPage = userRepository.findByIsDeletedFalse(pageable);
        
        // 转换为DTO
        return userPage.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public UserDTO updateUserProfile(String username, ProfileUpdateDTO profileUpdateDTO) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserException::userNotFound);
        
        checkUserNotDeleted(user.getId());

        if (profileUpdateDTO.getAvatar() != null) {
            user.setAvatar(profileUpdateDTO.getAvatar());
        }
        
        if (profileUpdateDTO.getProfile() != null) {
            user.setProfile(JsonUtils.toJson(profileUpdateDTO.getProfile()));
        }

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public Map<String, String> updateUserAvatar(String username, MultipartFile file) {
        // 检查用户是否存在
        User user = findByUsername(username)
                .orElseThrow(UserException::userNotFound);

        // 检查用户是否被软删除
        checkUserNotDeleted(user.getId());

        try {
            // 删除旧头像
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                try {
                    minioUtil.deleteUserAvatar(user.getAvatar());
                } catch (Exception e) {
                    // 删除旧头像失败不影响新头像上传，记录日志即可
                    log.warn("删除旧头像失败: {}", e.getMessage());
                }
            }

            // 上传新头像
            String avatarPath = minioUtil.uploadUserAvatar(file, user.getId());
            user.setAvatar(avatarPath);
            userRepository.save(user);

            // 生成访问URL
            String avatarUrl = minioUtil.getAvatarPreviewUrl(avatarPath, avatarUrlExpiry);

            // 返回头像信息
            Map<String, String> result = new HashMap<>();
            result.put("avatarPath", avatarPath);
            result.put("avatarUrl", avatarUrl);
            return result;
        } catch (Exception e) {
            log.error("上传头像失败", e);
            throw new UserException(400,"上传头像失败: " + e.getMessage());
        }
    }

    @Override
    public String getUserAvatarUrl(String userId) {
        User user = userRepository.findById(userId).orElseThrow(UserException::userNotFound);
        
        checkUserNotDeleted(user.getId());
        
        if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
            return SystemConstants.DEFAULT_AVATAR_URL;
        }
        
        try {
            return minioUtil.getAvatarPreviewUrl(user.getAvatar(), avatarUrlExpiry);
        } catch (Exception e) {
            log.error("获取头像URL失败", e);
            return SystemConstants.DEFAULT_AVATAR_URL;
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Set<String> getUserRoleIds(String userId) {
        try {
            Result<Set<String>> result = authServiceClient.getUserRoleIds(userId);
            if (result.getCode() == 200) {
                return result.getData() != null ? result.getData() : new HashSet<>();
            }
            return new HashSet<>();
        } catch (Exception e) {
            log.error("获取用户角色失败: {}", e.getMessage());
            return new HashSet<>();
        }
    }

    @Override
    @Transactional
    public void setUserRole(String targetUserId, String roleId, String currentUsername) {
        // 验证当前用户
        User currentUser = findByUsername(currentUsername)
                .orElseThrow(UserException::userNotFound);
        
        // 验证目标用户
        checkUserNotDeleted(targetUserId);
        
        // 获取当前用户角色
        Set<String> currentUserRoles = getUserRoleIds(currentUser.getId());
        
        // 检查权限
        if (!canAssignRole(currentUserRoles, roleId)) {
            throw UserException.insufficientPermissions();
        }
        
        try {
            authServiceClient.setUserRole(targetUserId, roleId);
            log.info("用户 {} 为用户 {} 设置角色 {}", currentUsername, targetUserId, roleId);
        } catch (Exception e) {
            log.error("设置用户角色失败: {}", e.getMessage());
            throw new UserException(400,"设置用户角色失败");
        }
    }

    /**
     * 将User实体转换为UserDTO
     */
    private UserDTO convertToDTO(User user) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        
        // 设置头像URL
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                String avatarUrl = minioUtil.getAvatarPreviewUrl(user.getAvatar(), avatarUrlExpiry);
                dto.setAvatarUrl(avatarUrl);
            } catch (Exception e) {
                // 头像URL生成失败，使用默认头像
                dto.setAvatarUrl(SystemConstants.DEFAULT_AVATAR_URL);
            }
        } else {
            // 没有头像，使用默认头像
            dto.setAvatarUrl(SystemConstants.DEFAULT_AVATAR_URL);
        }
        
        // 尝试解析JSON字符串为ProfileRequestDTO
        try {
            ProfileRequestDTO profileDTO = JsonUtils.fromJson(user.getProfile(), ProfileRequestDTO.class);
            dto.setProfile(profileDTO);
        } catch (Exception e) {
            // 如果解析失败，保存原始字符串
            dto.setProfile(user.getProfile());
        }
        
        dto.setRoles(getUserRoleIds(user.getId()));
        dto.setCreatedAt(dateFormat.format(user.getCreatedAt()));
        dto.setUpdatedAt(dateFormat.format(user.getUpdatedAt()));
        dto.setIsDeleted(user.getIsDeleted());

        return dto;
    }

    /**
     * 检查当前用户是否可以分配指定角色
     */
    private boolean canAssignRole(Set<String> currentUserRoles, String targetRoleId) {
        if (currentUserRoles.contains(SystemConstants.ROLE_ADMIN)) {
            return true;
        }

        if (currentUserRoles.contains(SystemConstants.ROLE_TEACHER)) {
            return SystemConstants.ROLE_TEACHER.equals(targetRoleId)
                    || SystemConstants.ROLE_ASSISTANT.equals(targetRoleId)
                    || SystemConstants.ROLE_STUDENT.equals(targetRoleId);
        }

        if (currentUserRoles.contains(SystemConstants.ROLE_ASSISTANT)) {
            return SystemConstants.ROLE_ASSISTANT.equals(targetRoleId)
                    || SystemConstants.ROLE_STUDENT.equals(targetRoleId);
        }

        return false;
    }

    /**
     * 检查用户是否未被删除
     */
    private void checkUserNotDeleted(String userIdOrName) {
        // 先尝试通过ID查找
        Optional<User> userById = userRepository.findById(userIdOrName);
        
        // 如果通过ID找不到，再尝试通过用户名查找
        Optional<User> userByName = userById.isPresent() ? userById : userRepository.findByUsername(userIdOrName);
        
        // 如果两种方式都找不到用户，抛出用户不存在异常
        User user = userByName.orElseThrow(UserException::userNotFound);

        if (user.getIsDeleted()) {
            throw UserException.userDeleted();
        }
    }
}
