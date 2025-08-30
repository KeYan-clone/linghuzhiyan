package org.linghu.experiment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.experiment.dto.Result;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.service.ExperimentAssignmentService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExperimentAssignmentController 单元测试 - 使用纯单元测试方式
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("实验分配控制器测试")
class ExperimentAssignmentControllerTest {

    @Mock
    private ExperimentAssignmentService experimentAssignmentService;

    @InjectMocks
    private ExperimentAssignmentController experimentAssignmentController;

    private List<UserDTO> userList;
    private Map<String, Object> singleUserAssignment;
    private Map<String, Object> multipleUserAssignment;
    private Map<String, Object> invalidAssignment;

    @BeforeEach
    void setUp() {
        // 创建测试用户列表
        UserDTO user1 = new UserDTO();
        user1.setId("user1");
        user1.setUsername("student1");
        user1.setEmail("student1@test.com");

        UserDTO user2 = new UserDTO();
        user2.setId("user2");
        user2.setUsername("student2");
        user2.setEmail("student2@test.com");

        userList = Arrays.asList(user1, user2);

        // 创建单个用户分配请求
        singleUserAssignment = new HashMap<>();
        singleUserAssignment.put("userId", "user1");

        // 创建多个用户分配请求
        multipleUserAssignment = new HashMap<>();
        multipleUserAssignment.put("userIds", Arrays.asList("user1", "user2"));

        // 创建无效分配请求
        invalidAssignment = new HashMap<>();
        invalidAssignment.put("invalidKey", "invalidValue");
    }

    @Nested
    @DisplayName("分配实验任务测试")
    class AssignTaskTests {

        @Test
        @DisplayName("成功分配任务给单个用户")
        void shouldAssignTaskToSingleUserSuccessfully() {
            // given
            doNothing().when(experimentAssignmentService).assignTask("task123", "user1");

            // when
            Result<Void> response = experimentAssignmentController.assignTask("task123", singleUserAssignment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNull();

            verify(experimentAssignmentService).assignTask("task123", "user1");
            verify(experimentAssignmentService, never()).batchAssignTask(any(), any());
        }

        @Test
        @DisplayName("成功批量分配任务给多个用户")
        void shouldBatchAssignTaskToMultipleUsersSuccessfully() {
            // given
            List<String> userIds = Arrays.asList("user1", "user2");
            doNothing().when(experimentAssignmentService).batchAssignTask("task123", userIds);

            // when
            Result<Void> response = experimentAssignmentController.assignTask("task123", multipleUserAssignment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNull();

            verify(experimentAssignmentService).batchAssignTask("task123", userIds);
            verify(experimentAssignmentService, never()).assignTask(any(), any());
        }

        @Test
        @DisplayName("分配任务时参数无效")
        void shouldHandleInvalidAssignmentParameters() {
            // when
            Result<Void> response = experimentAssignmentController.assignTask("task123", invalidAssignment);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(400);
            assertThat(response.getMessage()).isEqualTo("请提供userId或userIds参数");
            assertThat(response.getData()).isNull();

            verify(experimentAssignmentService, never()).assignTask(any(), any());
            verify(experimentAssignmentService, never()).batchAssignTask(any(), any());
        }

        @Test
        @DisplayName("分配任务给单个用户时服务异常")
        void shouldHandleServiceExceptionWhenAssigningSingleUser() {
            // given
            doThrow(new RuntimeException("分配失败")).when(experimentAssignmentService).assignTask("task123", "user1");

            // when & then
            try {
                experimentAssignmentController.assignTask("task123", singleUserAssignment);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("分配失败");
            }

            verify(experimentAssignmentService).assignTask("task123", "user1");
        }

        @Test
        @DisplayName("批量分配任务时服务异常")
        void shouldHandleServiceExceptionWhenBatchAssigning() {
            // given
            List<String> userIds = Arrays.asList("user1", "user2");
            doThrow(new RuntimeException("批量分配失败")).when(experimentAssignmentService).batchAssignTask("task123", userIds);

            // when & then
            try {
                experimentAssignmentController.assignTask("task123", multipleUserAssignment);
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("批量分配失败");
            }

            verify(experimentAssignmentService).batchAssignTask("task123", userIds);
        }

        @Test
        @DisplayName("成功分配任务给全部学生")
        void shouldAssignTaskToAllStudentsSuccessfully() {
            // given
            doNothing().when(experimentAssignmentService).assignTaskToAllStudents("task123");

            // when
            Result<Void> response = experimentAssignmentController.assignTaskToAllStudents("task123");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNull();

            verify(experimentAssignmentService).assignTaskToAllStudents("task123");
        }

        @Test
        @DisplayName("分配任务给全部学生时服务异常")
        void shouldHandleServiceExceptionWhenAssigningToAllStudents() {
            // given
            doThrow(new RuntimeException("分配给全部学生失败")).when(experimentAssignmentService).assignTaskToAllStudents("task123");

            // when & then
            try {
                experimentAssignmentController.assignTaskToAllStudents("task123");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("分配给全部学生失败");
            }

            verify(experimentAssignmentService).assignTaskToAllStudents("task123");
        }
    }

    @Nested
    @DisplayName("获取任务分配测试")
    class GetTaskAssignmentTests {

        @Test
        @DisplayName("成功获取任务分配列表")
        void shouldGetTaskAssignmentsSuccessfully() {
            // given
            when(experimentAssignmentService.getTaskAssignments("task123")).thenReturn(userList);

            // when
            Result<List<UserDTO>> response = experimentAssignmentController.getTaskAssignments("task123");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).hasSize(2);
            assertThat(response.getData()).isEqualTo(userList);

            verify(experimentAssignmentService).getTaskAssignments("task123");
        }

        @Test
        @DisplayName("获取不存在任务的分配列表")
        void shouldHandleGetAssignmentsForNonexistentTask() {
            // given
            when(experimentAssignmentService.getTaskAssignments("nonexistent"))
                    .thenThrow(new RuntimeException("任务未找到"));

            // when & then
            try {
                experimentAssignmentController.getTaskAssignments("nonexistent");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("任务未找到");
            }

            verify(experimentAssignmentService).getTaskAssignments("nonexistent");
        }

        @Test
        @DisplayName("获取空的任务分配列表")
        void shouldHandleEmptyTaskAssignments() {
            // given
            when(experimentAssignmentService.getTaskAssignments("task123")).thenReturn(Arrays.asList());

            // when
            Result<List<UserDTO>> response = experimentAssignmentController.getTaskAssignments("task123");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNotNull();
            assertThat(response.getData()).isEmpty();

            verify(experimentAssignmentService).getTaskAssignments("task123");
        }
    }

    @Nested
    @DisplayName("取消任务分配测试")
    class RemoveTaskAssignmentTests {

        @Test
        @DisplayName("成功取消任务分配")
        void shouldRemoveTaskAssignmentSuccessfully() {
            // given
            doNothing().when(experimentAssignmentService).removeTaskAssignment("task123", "user1");

            // when
            Result<Void> response = experimentAssignmentController.removeTaskAssignment("task123", "user1");

            // then
            assertThat(response).isNotNull();
            assertThat(response.getCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("success");
            assertThat(response.getData()).isNull();

            verify(experimentAssignmentService).removeTaskAssignment("task123", "user1");
        }

        @Test
        @DisplayName("取消不存在的任务分配")
        void shouldHandleRemoveNonexistentTaskAssignment() {
            // given
            doThrow(new RuntimeException("分配记录未找到")).when(experimentAssignmentService).removeTaskAssignment("nonexistent", "user1");

            // when & then
            try {
                experimentAssignmentController.removeTaskAssignment("nonexistent", "user1");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("分配记录未找到");
            }

            verify(experimentAssignmentService).removeTaskAssignment("nonexistent", "user1");
        }

        @Test
        @DisplayName("取消任务分配时服务异常")
        void shouldHandleServiceExceptionWhenRemoving() {
            // given
            doThrow(new RuntimeException("取消分配失败")).when(experimentAssignmentService).removeTaskAssignment("task123", "user1");

            // when & then
            try {
                experimentAssignmentController.removeTaskAssignment("task123", "user1");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("取消分配失败");
            }

            verify(experimentAssignmentService).removeTaskAssignment("task123", "user1");
        }

        @Test
        @DisplayName("使用无效参数取消任务分配")
        void shouldHandleInvalidParametersForRemoval() {
            // given
            doThrow(new RuntimeException("参数无效")).when(experimentAssignmentService).removeTaskAssignment("", "");

            // when & then
            try {
                experimentAssignmentController.removeTaskAssignment("", "");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("参数无效");
            }

            verify(experimentAssignmentService).removeTaskAssignment("", "");
        }
    }
}
