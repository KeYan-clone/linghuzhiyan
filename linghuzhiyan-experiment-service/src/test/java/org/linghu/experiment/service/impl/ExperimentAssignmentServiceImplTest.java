package org.linghu.experiment.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.experiment.client.UserServiceClient;
import org.linghu.experiment.domain.ExperimentAssignment;
import org.linghu.experiment.domain.ExperimentTask;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.repository.ExperimentAssignmentRepository;
import org.linghu.experiment.repository.ExperimentRepository;
import org.linghu.experiment.repository.ExperimentTaskRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExperimentAssignmentServiceImpl 单元测试类
 */
@ExtendWith(MockitoExtension.class)
class ExperimentAssignmentServiceImplTest {

    @Mock
    private ExperimentAssignmentRepository assignmentRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private ExperimentTaskRepository experimentTaskRepository;

    @InjectMocks
    private ExperimentAssignmentServiceImpl assignmentService;

    private ExperimentTask testTask;
    private UserDTO testStudent;
    private UserDTO testTeacher;
    private ExperimentAssignment testAssignment;

    @BeforeEach
    void setUp() {
        // 创建测试数据
        testTask = ExperimentTask.builder()
                .id("task1")
                .experimentId("experiment1")
                .title("Test Task")
                .description("Test Description")
                .required(true)
                .orderNum(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testStudent = new UserDTO();
        testStudent.setId("user1");
        testStudent.setUsername("student1");
        testStudent.setEmail("student1@test.com");
        testStudent.setRoles(Set.of("STUDENT"));

        testTeacher = new UserDTO();
        testTeacher.setId("user2");
        testTeacher.setUsername("teacher1");
        testTeacher.setEmail("teacher1@test.com");
        testTeacher.setRoles(Set.of("TEACHER"));

        testAssignment = new ExperimentAssignment();
        testAssignment.setId("assignment1");
        testAssignment.setTaskId("task1");
        testAssignment.setUserId("user1");
        testAssignment.setAssignedAt(new Date());
    }

    // assignTask 方法的正面测试
    @Test
    void assignTask_WithValidTaskAndStudentUser_ShouldAssignSuccessfully() {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUserByIdInExp("user1")).thenReturn(testStudent);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user1")).thenReturn(false);
        when(assignmentRepository.save(any(ExperimentAssignment.class))).thenReturn(testAssignment);

        // When
        assertDoesNotThrow(() -> assignmentService.assignTask("task1", "user1"));

        // Then
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getUserByIdInExp("user1");
        verify(assignmentRepository).existsByTaskIdAndUserId("task1", "user1");
        verify(assignmentRepository).save(any(ExperimentAssignment.class));
    }

    // assignTask 方法的反面测试 - 任务不存在
    @Test
    void assignTask_WithNonExistentTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.assignTask("nonexistent", "user1"));
        assertEquals("实验任务不存在", exception.getMessage());
        
        verify(experimentTaskRepository).findById("nonexistent");
        verify(userServiceClient, never()).getUserByIdInExp(anyString());
        verify(assignmentRepository, never()).save(any());
    }

    // assignTask 方法的反面测试 - 用户不存在
    @Test
    void assignTask_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUserByIdInExp("nonexistent")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.assignTask("task1", "nonexistent"));
        assertEquals("用户不存在", exception.getMessage());
        
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getUserByIdInExp("nonexistent");
        verify(assignmentRepository, never()).save(any());
    }

    // assignTask 方法的反面测试 - 已分配
    @Test
    void assignTask_WithAlreadyAssignedTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUserByIdInExp("user1")).thenReturn(testStudent);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user1")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.assignTask("task1", "user1"));
        assertEquals("该实验任务已分配给此用户", exception.getMessage());
        
        verify(assignmentRepository, never()).save(any());
    }

    // assignTask 方法的反面测试 - 非学生用户
    @Test
    void assignTask_WithNonStudentUser_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUserByIdInExp("user2")).thenReturn(testTeacher);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user2")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.assignTask("task1", "user2"));
        assertEquals("只能将实验任务分配给学生角色的用户", exception.getMessage());
        
        verify(assignmentRepository, never()).save(any());
    }

    // batchAssignTask 方法的正面测试
    @Test
    void batchAssignTask_WithValidTaskAndStudents_ShouldAssignSuccessfully() {
        // Given
        List<String> userIds = Arrays.asList("user1", "user3");
        UserDTO student2 = new UserDTO();
        student2.setId("user3");
        student2.setUsername("student2");
        student2.setRoles(Set.of("STUDENT"));
        
        List<UserDTO> users = Arrays.asList(testStudent, student2);
        
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUsersByIdsInExp(userIds)).thenReturn(users);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user1")).thenReturn(false);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user3")).thenReturn(false);
        when(assignmentRepository.save(any(ExperimentAssignment.class))).thenReturn(testAssignment);

        // When
        assertDoesNotThrow(() -> assignmentService.batchAssignTask("task1", userIds));

        // Then
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getUsersByIdsInExp(userIds);
        verify(assignmentRepository, times(2)).save(any(ExperimentAssignment.class));
    }

    // batchAssignTask 方法的反面测试 - 任务不存在
    @Test
    void batchAssignTask_WithNonExistentTask_ShouldThrowException() {
        // Given
        List<String> userIds = Arrays.asList("user1", "user3");
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.batchAssignTask("nonexistent", userIds));
        assertEquals("实验任务不存在", exception.getMessage());
        
        verify(experimentTaskRepository).findById("nonexistent");
        verify(userServiceClient, never()).getUsersByIdsInExp(anyList());
        verify(assignmentRepository, never()).save(any());
    }

    // getTaskAssignments 方法的正面测试
    @Test
    void getTaskAssignments_WithValidTask_ShouldReturnAssignedUsers() {
        // Given
        List<ExperimentAssignment> assignments = Arrays.asList(testAssignment);
        List<String> userIds = Arrays.asList("user1");
        List<UserDTO> users = Arrays.asList(testStudent);
        
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(assignmentRepository.findByTaskId("task1")).thenReturn(assignments);
        when(userServiceClient.getUsersByIdsInExp(userIds)).thenReturn(users);

        // When
        List<UserDTO> result = assignmentService.getTaskAssignments("task1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("user1", result.get(0).getId());
        assertEquals("student1", result.get(0).getUsername());
        
        verify(experimentTaskRepository).findById("task1");
        verify(assignmentRepository).findByTaskId("task1");
        verify(userServiceClient).getUsersByIdsInExp(userIds);
    }

    // getTaskAssignments 方法的反面测试 - 任务不存在
    @Test
    void getTaskAssignments_WithNonExistentTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.getTaskAssignments("nonexistent"));
        assertEquals("实验任务不存在", exception.getMessage());
        
        verify(experimentTaskRepository).findById("nonexistent");
        verify(assignmentRepository, never()).findByTaskId(anyString());
    }

    // assignTaskToAllStudents 方法的正面测试
    @Test
    void assignTaskToAllStudents_WithValidTask_ShouldAssignToAllStudents() {
        // Given
        List<UserDTO> allUsers = Arrays.asList(testStudent, testTeacher);
        
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getAllUsersInExp()).thenReturn(allUsers);
        when(userServiceClient.getUsersByIdsInExp(Arrays.asList("user1"))).thenReturn(Arrays.asList(testStudent));
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user1")).thenReturn(false);
        when(assignmentRepository.save(any(ExperimentAssignment.class))).thenReturn(testAssignment);

        // When
        assertDoesNotThrow(() -> assignmentService.assignTaskToAllStudents("task1"));

        // Then
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getAllUsersInExp();
        verify(userServiceClient).getUsersByIdsInExp(Arrays.asList("user1"));
        verify(assignmentRepository).save(any(ExperimentAssignment.class));
    }

    // assignTaskToAllStudents 方法的反面测试 - 任务不存在
    @Test
    void assignTaskToAllStudents_WithNonExistentTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.assignTaskToAllStudents("nonexistent"));
        assertEquals("实验任务不存在", exception.getMessage());
        
        verify(experimentTaskRepository).findById("nonexistent");
        verify(userServiceClient).getAllUsersInExp();
    }

    // removeTaskAssignment 方法的正面测试
    @Test
    void removeTaskAssignment_WithExistingAssignment_ShouldRemoveSuccessfully() {
        // Given
        when(assignmentRepository.findByTaskIdAndUserId("task1", "user1"))
                .thenReturn(Optional.of(testAssignment));

        // When
        assertDoesNotThrow(() -> assignmentService.removeTaskAssignment("task1", "user1"));

        // Then
        verify(assignmentRepository).findByTaskIdAndUserId("task1", "user1");
        verify(assignmentRepository).delete(testAssignment);
    }

    // removeTaskAssignment 方法的反面测试 - 分配不存在
    @Test
    void removeTaskAssignment_WithNonExistentAssignment_ShouldThrowException() {
        // Given
        when(assignmentRepository.findByTaskIdAndUserId("task1", "user1"))
                .thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> assignmentService.removeTaskAssignment("task1", "user1"));
        assertEquals("未找到该实验任务分配", exception.getMessage());
        
        verify(assignmentRepository).findByTaskIdAndUserId("task1", "user1");
        verify(assignmentRepository, never()).delete(any());
    }

    // batchAssignTask 方法的反面测试 - 部分用户分配失败但继续处理
    @Test
    void batchAssignTask_WithMixedUsers_ShouldContinueProcessingAfterFailure() {
        // Given
        List<String> userIds = Arrays.asList("user1", "user2"); // user1是学生，user2是老师
        List<UserDTO> users = Arrays.asList(testStudent, testTeacher);
        
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getUsersByIdsInExp(userIds)).thenReturn(users);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user1")).thenReturn(false);
        when(assignmentRepository.existsByTaskIdAndUserId("task1", "user2")).thenReturn(false);
        when(assignmentRepository.save(any(ExperimentAssignment.class))).thenReturn(testAssignment);

        // When
        assertDoesNotThrow(() -> assignmentService.batchAssignTask("task1", userIds));

        // Then
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getUsersByIdsInExp(userIds);
        // 只有学生用户应该被分配，老师用户会被跳过
        verify(assignmentRepository, times(1)).save(any(ExperimentAssignment.class));
    }

    // getTaskAssignments 方法的正面测试 - 无分配用户
    @Test
    void getTaskAssignments_WithNoAssignments_ShouldReturnEmptyList() {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(assignmentRepository.findByTaskId("task1")).thenReturn(Collections.emptyList());
        // When
        List<UserDTO> result = assignmentService.getTaskAssignments("task1");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(experimentTaskRepository).findById("task1");
        verify(assignmentRepository).findByTaskId("task1");
    }

    // assignTaskToAllStudents 方法的正面测试 - 无学生用户
    @Test
    void assignTaskToAllStudents_WithNoStudents_ShouldCompleteWithoutAssignments() {
        // Given
        List<UserDTO> allUsers = Arrays.asList(testTeacher); // 只有老师，没有学生
        
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(userServiceClient.getAllUsersInExp()).thenReturn(allUsers);

        // When
        assertDoesNotThrow(() -> assignmentService.assignTaskToAllStudents("task1"));

        // Then
        verify(experimentTaskRepository).findById("task1");
        verify(userServiceClient).getAllUsersInExp();
        verify(assignmentRepository, never()).save(any());
    }
}
