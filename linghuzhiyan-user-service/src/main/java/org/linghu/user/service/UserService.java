package org.linghu.user.service;

import org.linghu.user.domain.User;
import org.linghu.user.dto.ProfileUpdateDTO;
import org.linghu.user.dto.UserDTO;
import org.linghu.user.dto.UserRegistrationDTO;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 用户服务接口
 * 定义与用户相关的业务操作
 */
public interface UserService {
    
    /**
     * 用户注册
     * 
     * @param registrationDTO 注册信息
     * @return 用户DTO
     */
    UserDTO registerUser(UserRegistrationDTO registrationDTO);
    
    /**
     * 删除用户
     * 
     * @param userId 要删除的用户ID
     * @param currentUsername 当前登录用户的用户名
     */
    void deleteUser(String userId, String currentUsername);

    /**
     * 根据ID获取用户
     * 
     * @param id 用户ID
     * @return 用户DTO
     */
    UserDTO getUserById(String id);
    
    /**
     * 根据用户名获取用户
     * 
     * @param username 用户名
     * @return 用户DTO
     */
    UserDTO getUserByUsername(String username);

    /**
     * 批量获取用户
     *
     * @param userIds 用户ID列表
     * @return 用户DTO列表
     */
    List<UserDTO> getUsersByIds(List<String> userIds);

    /**
     * 获取所有用户
     *
     * @return 所有用户DTO列表
     */
    List<UserDTO> getAllUsers();
    /**
     * 修改密码
     * 
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String username, String oldPassword, String newPassword);
    
    /**
     * 分页查询用户
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 用户分页列表
     */
    Page<UserDTO> listUsers(int pageNum, int pageSize);
    
    /**
     * 更新用户资料
     * 
     * @param username 用户名
     * @param profileUpdateDTO 用户资料更新信息
     * @return 更新后的用户DTO
     */
    UserDTO updateUserProfile(String username, ProfileUpdateDTO profileUpdateDTO);
    
    /**
     * 更新用户头像
     * 
     * @param username 用户名
     * @param file 头像文件
     * @return 包含头像路径和URL的Map
     */
    Map<String, String> updateUserAvatar(String username, MultipartFile file);
    
    /**
     * 获取用户头像URL
     * 
     * @param userId 用户ID
     * @return 头像访问URL
     */
    String getUserAvatarUrl(String userId);
    
    /**
     * 根据用户名查找用户
     * 
     * @param username 用户名
     * @return 用户可选对象
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户可选对象
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否已存在
     * 
     * @param username 用户名
     * @return 如果存在则返回true
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱
     * @return 如果存在则返回true
     */
    boolean existsByEmail(String email);
    
    /**
     * 获取用户角色
     * 
     * @param userId 用户ID
     * @return 角色ID集合
     */
    Set<String> getUserRoleIds(String userId);
    
    /**
     * 设置用户角色（权限分配）
     * 只能分配同级或更低级的权限
     * 
     * @param targetUserId 目标用户ID
     * @param roleId 要分配的角色ID
     * @param currentUsername 当前操作用户名
     */
    void setUserRole(String targetUserId, String roleId, String currentUsername);
}
