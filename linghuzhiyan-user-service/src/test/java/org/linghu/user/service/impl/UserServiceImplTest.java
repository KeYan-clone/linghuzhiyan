package org.linghu.user.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.user.client.AuthServiceClient;
import org.linghu.user.constants.SystemConstants;
import org.linghu.user.domain.User;
import org.linghu.user.dto.*;
import org.linghu.user.exception.UserException;
import org.linghu.user.repository.UserRepository;
import org.linghu.user.util.MinioUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * UserServiceImpl 单元测试类
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthServiceClient authServiceClient;

    @Mock
    private MinioUtil minioUtil;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserRegistrationDTO registrationDTO;
    private ProfileUpdateDTO profileUpdateDTO;
    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // 设置配置值
        ReflectionTestUtils.setField(userService, "avatarUrlExpiry", 3600);

        // 创建测试用户
        testUser = new User();
        testUser.setId("user-123");
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setProfile("{}");
        testUser.setIsDeleted(false);
        testUser.setCreatedAt(new Date());
        testUser.setUpdatedAt(new Date());

        // 创建注册DTO
        registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("newuser");
        registrationDTO.setEmail("new@example.com");
        registrationDTO.setPassword("password123");

        // 创建资料更新DTO
        profileUpdateDTO = new ProfileUpdateDTO();

        // 创建模拟文件
        mockFile = mock(MultipartFile.class);
    }

    // ===== 用户注册测试 =====
    @Test
    void registerUser_Success() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(authServiceClient.setUserRole(anyString(), anyString())).thenReturn(Result.success());

        // When
        UserDTO result = userService.registerUser(registrationDTO);

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("testuser", result.getUsername());
        verify(userRepository).save(any(User.class));
        verify(authServiceClient).setUserRole(anyString(), eq(SystemConstants.ROLE_STUDENT));
    }

    @Test
    void registerUser_Failure_UsernameExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        // When & Then
        assertThrows(UserException.class, () -> userService.registerUser(registrationDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerUser_Failure_EmailExists() {
        // Given
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);

        // When & Then
        assertThrows(UserException.class, () -> userService.registerUser(registrationDTO));
        verify(userRepository, never()).save(any(User.class));
    }

    // ===== 删除用户测试 =====
    @Test
    void deleteUser_Success_AdminDeletesSelf() {
        // Given
        User adminUser = createUserWithRoles("admin", Set.of(SystemConstants.ROLE_ADMIN));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        mockUserRoles("admin-id", Set.of(SystemConstants.ROLE_ADMIN));
        mockUserRoles("user-123", Set.of(SystemConstants.ROLE_STUDENT));

        // When
        userService.deleteUser("user-123", "admin");

        // Then
        verify(userRepository).save(argThat(user -> user.getIsDeleted()));
    }

    @Test
    void deleteUser_Failure_InsufficientPermissions() {
        // Given
        User studentUser = createUserWithRoles("student", Set.of(SystemConstants.ROLE_STUDENT));
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        mockUserRoles("student-id", Set.of(SystemConstants.ROLE_STUDENT));

        // When & Then
        assertThrows(UserException.class, () -> userService.deleteUser("user-123", "student"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("user-123")).thenReturn(Optional.of(testUser));


        // When & Then
        assertThrows(UserException.class, () -> userService.deleteUser("user-123", "admin"));
    }

    // ===== 获取用户测试 =====
    @Test
    void getUserById_Success() {
        // Given
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        mockUserRoles("user-123", Set.of(SystemConstants.ROLE_STUDENT));

        // When
        UserDTO result = userService.getUserById("user-123");

        // Then
        assertNotNull(result);
        assertEquals("user-123", result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserById_Failure_UserNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.getUserById("nonexistent"));
    }

    @Test
    void getUserByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        mockUserRoles("user-123", Set.of(SystemConstants.ROLE_STUDENT));

        // When
        UserDTO result = userService.getUserByUsername("testuser");

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getUserByUsername_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.getUserByUsername("nonexistent"));
    }

    // ===== 修改密码测试 =====
    @Test
    void changePassword_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldpass", "encoded-password")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("new-encoded-password");

        // When
        userService.changePassword("testuser", "oldpass", "newpass");

        // Then
        verify(userRepository).save(argThat(user -> "new-encoded-password".equals(user.getPassword())));
    }

    @Test
    void changePassword_Failure_WrongOldPassword() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongpass", "encoded-password")).thenReturn(false);

        // When & Then
        assertThrows(UserException.class, () -> userService.changePassword("testuser", "wrongpass", "newpass"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.changePassword("nonexistent", "oldpass", "newpass"));
    }

    // ===== 分页查询用户测试 =====
    @Test
    void listUsers_Success() {
        // Given
        List<User> users = Arrays.asList(testUser);
        Page<User> userPage = new PageImpl<>(users);
        when(userRepository.findByIsDeletedFalse(any(Pageable.class))).thenReturn(userPage);
        mockUserRoles("user-123", Set.of(SystemConstants.ROLE_STUDENT));

        // When
        Page<UserDTO> result = userService.listUsers(1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).getUsername());
    }

    // ===== 更新个人资料测试 =====
    @Test
    void updateUserProfile_Success() {
        // Given
        profileUpdateDTO.setAvatar("new-avatar.jpg");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        mockUserRoles("user-123", Set.of(SystemConstants.ROLE_STUDENT));

        // When
        UserDTO result = userService.updateUserProfile("testuser", profileUpdateDTO);

        // Then
        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserProfile_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.updateUserProfile("nonexistent", profileUpdateDTO));
    }

    // ===== 上传头像测试 =====
    @Test
    void updateUserAvatar_Success() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(minioUtil.uploadUserAvatar(mockFile, "user-123")).thenReturn("avatars/user-123.jpg");
        when(minioUtil.getAvatarPreviewUrl("avatars/user-123.jpg", 3600)).thenReturn("http://example.com/avatar.jpg");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        Map<String, String> result = userService.updateUserAvatar("testuser", mockFile);

        // Then
        assertNotNull(result);
        assertEquals("avatars/user-123.jpg", result.get("avatarPath"));
        assertEquals("http://example.com/avatar.jpg", result.get("avatarUrl"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUserAvatar_Failure_UserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.updateUserAvatar("nonexistent", mockFile));
    }

    @Test
    void updateUserAvatar_Failure_UploadException() throws Exception {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(minioUtil.uploadUserAvatar(mockFile, "user-123")).thenThrow(new RuntimeException("Upload failed"));

        // When & Then
        assertThrows(UserException.class, () -> userService.updateUserAvatar("testuser", mockFile));
    }

    // ===== 获取头像URL测试 =====
    @Test
    void getUserAvatarUrl_Success_WithAvatar() throws Exception {
        // Given
        testUser.setAvatar("avatars/user-123.jpg");
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        when(minioUtil.getAvatarPreviewUrl("avatars/user-123.jpg", 3600)).thenReturn("http://example.com/avatar.jpg");

        // When
        String result = userService.getUserAvatarUrl("user-123");

        // Then
        assertEquals("http://example.com/avatar.jpg", result);
    }

    @Test
    void getUserAvatarUrl_Success_NoAvatar() {
        // Given
        testUser.setAvatar(null);
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));

        // When
        String result = userService.getUserAvatarUrl("user-123");

        // Then
        assertEquals(SystemConstants.DEFAULT_AVATAR_URL, result);
    }

    @Test
    void getUserAvatarUrl_Failure_UserNotFound() {
        // Given
        when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> userService.getUserAvatarUrl("nonexistent"));
    }

    // ===== 检查用户名/邮箱存在性测试 =====
    @Test
    void existsByUsername_Success_True() {
        // Given
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // When
        boolean result = userService.existsByUsername("testuser");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByUsername_Success_False() {
        // Given
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // When
        boolean result = userService.existsByUsername("nonexistent");

        // Then
        assertFalse(result);
    }

    @Test
    void existsByEmail_Success_True() {
        // Given
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // When
        boolean result = userService.existsByEmail("test@example.com");

        // Then
        assertTrue(result);
    }

    @Test
    void existsByEmail_Success_False() {
        // Given
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // When
        boolean result = userService.existsByEmail("nonexistent@example.com");

        // Then
        assertFalse(result);
    }

    // ===== 查找用户测试 =====
    @Test
    void findByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByUsername("testuser");

        // Then
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void findByUsername_Failure_NotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByUsername("nonexistent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_Success() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.findByEmail("test@example.com");

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void findByEmail_Failure_NotFound() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.findByEmail("nonexistent@example.com");

        // Then
        assertFalse(result.isPresent());
    }

    // ===== 获取用户角色测试 =====
    @Test
    void getUserRoleIds_Success() {
        // Given
        Set<String> roles = Set.of(SystemConstants.ROLE_STUDENT);
        Result<Set<String>> result = Result.success(roles);
        when(authServiceClient.getUserRoleIds("user-123")).thenReturn(result);

        // When
        Set<String> userRoles = userService.getUserRoleIds("user-123");

        // Then
        assertEquals(roles, userRoles);
    }

    @Test
    void getUserRoleIds_Failure_ServiceException() {
        // Given
        when(authServiceClient.getUserRoleIds("user-123")).thenThrow(new RuntimeException("Service error"));

        // When
        Set<String> userRoles = userService.getUserRoleIds("user-123");

        // Then
        assertTrue(userRoles.isEmpty());
    }

    // ===== 设置用户角色测试 =====
    @Test
    void setUserRole_Success_AdminSetsStudentRole() {
        // Given
        User adminUser = createUserWithRoles("admin", Set.of(SystemConstants.ROLE_ADMIN));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        mockUserRoles("admin-id", Set.of(SystemConstants.ROLE_ADMIN));
        when(authServiceClient.setUserRole("user-123", SystemConstants.ROLE_STUDENT)).thenReturn(Result.success());

        // When
        userService.setUserRole("user-123", SystemConstants.ROLE_STUDENT, "admin");

        // Then
        verify(authServiceClient).setUserRole("user-123", SystemConstants.ROLE_STUDENT);
    }

    @Test
    void setUserRole_Failure_InsufficientPermissions() {
        // Given
        User studentUser = createUserWithRoles("student", Set.of(SystemConstants.ROLE_STUDENT));
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(studentUser));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(testUser));
        mockUserRoles("student-id", Set.of(SystemConstants.ROLE_STUDENT));

        // When & Then
        assertThrows(UserException.class, () -> 
            userService.setUserRole("user-123", SystemConstants.ROLE_ADMIN, "student"));
        verify(authServiceClient, never()).setUserRole(anyString(), anyString());
    }

    @Test
    void setUserRole_Failure_CurrentUserNotFound() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserException.class, () -> 
            userService.setUserRole("user-123", SystemConstants.ROLE_STUDENT, "nonexistent"));
    }

    // ===== 辅助方法 =====
    private User createUserWithRoles(String username, Set<String> roles) {
        User user = new User();
        user.setId(username + "-id");
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setIsDeleted(false);
        return user;
    }

    private void mockUserRoles(String userId, Set<String> roles) {
        Result<Set<String>> result = Result.success(roles);
        when(authServiceClient.getUserRoleIds(userId)).thenReturn(result);
    }
}
