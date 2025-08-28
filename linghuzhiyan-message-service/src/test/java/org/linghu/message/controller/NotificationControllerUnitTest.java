package org.linghu.message.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.message.service.MessageService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * NotificationController 纯单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("通知控制器纯单元测试")
class NotificationControllerUnitTest {

    @Mock
    private MessageService messageService;

    private NotificationController notificationController;

    private NotificationController.SystemNotificationRequest systemNotificationRequest;
    private NotificationController.ExperimentNotificationRequest experimentNotificationRequest;
    private NotificationController.GradeNotificationRequest gradeNotificationRequest;

    @BeforeEach
    void setUp() {
        notificationController = new NotificationController(messageService);
        
        // 创建系统通知请求
        systemNotificationRequest = new NotificationController.SystemNotificationRequest();
        systemNotificationRequest.setTitle("系统维护通知");
        systemNotificationRequest.setContent("系统将于今晚进行维护，请提前保存工作");
        systemNotificationRequest.setReceiverIds(Arrays.asList("user1", "user2", "user3"));

        // 创建实验通知请求
        experimentNotificationRequest = new NotificationController.ExperimentNotificationRequest();
        experimentNotificationRequest.setTitle("新实验发布");
        experimentNotificationRequest.setContent("算法设计实验已发布，请及时完成");
        experimentNotificationRequest.setExperimentId("exp123");
        experimentNotificationRequest.setReceiverIds(Arrays.asList("student1", "student2"));

        // 创建成绩通知请求
        gradeNotificationRequest = new NotificationController.GradeNotificationRequest();
        gradeNotificationRequest.setTitle("实验成绩通知");
        gradeNotificationRequest.setContent("您的实验成绩为85分");
        gradeNotificationRequest.setExperimentId("exp123");
        gradeNotificationRequest.setReceiverId("student1");
    }

    @Nested
    @DisplayName("系统通知发送测试")
    class SystemNotificationTests {

        @Test
        @DisplayName("成功发送系统通知")
        void shouldSendSystemNotificationSuccessfully() {
            // given
            doNothing().when(messageService).sendSystemNotification(
                    anyString(), anyString(), anyList());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendSystemNotification(systemNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("系统通知发送成功");
            assertThat(responseBody.get("receiverCount")).isEqualTo(3);

            verify(messageService).sendSystemNotification(
                    "系统维护通知",
                    "系统将于今晚进行维护，请提前保存工作",
                    Arrays.asList("user1", "user2", "user3")
            );
        }

        @Test
        @DisplayName("发送系统通知时服务异常")
        void shouldHandleServiceExceptionForSystemNotification() {
            // given
            doThrow(new RuntimeException("发送失败"))
                    .when(messageService).sendSystemNotification(anyString(), anyString(), anyList());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendSystemNotification(systemNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(false);
            assertThat(responseBody.get("message")).isEqualTo("发送系统通知失败: 发送失败");

            verify(messageService).sendSystemNotification(anyString(), anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("实验通知发送测试")
    class ExperimentNotificationTests {

        @Test
        @DisplayName("成功发送实验通知")
        void shouldSendExperimentNotificationSuccessfully() {
            // given
            doNothing().when(messageService).sendExperimentNotification(
                    anyString(), anyString(), anyString(), anyList());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendExperimentNotification(experimentNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("实验通知发送成功");
            assertThat(responseBody.get("experimentId")).isEqualTo("exp123");
            assertThat(responseBody.get("receiverCount")).isEqualTo(2);

            verify(messageService).sendExperimentNotification(
                    "新实验发布",
                    "算法设计实验已发布，请及时完成",
                    "exp123",
                    Arrays.asList("student1", "student2")
            );
        }

        @Test
        @DisplayName("发送实验通知时服务异常")
        void shouldHandleServiceExceptionForExperimentNotification() {
            // given
            doThrow(new RuntimeException("实验不存在"))
                    .when(messageService).sendExperimentNotification(
                            anyString(), anyString(), anyString(), anyList());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendExperimentNotification(experimentNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(false);
            assertThat(responseBody.get("message")).isEqualTo("发送实验通知失败: 实验不存在");

            verify(messageService).sendExperimentNotification(anyString(), anyString(), anyString(), anyList());
        }
    }

    @Nested
    @DisplayName("成绩通知发送测试")
    class GradeNotificationTests {

        @Test
        @DisplayName("成功发送成绩通知")
        void shouldSendGradeNotificationSuccessfully() {
            // given
            doNothing().when(messageService).sendGradeNotification(
                    anyString(), anyString(), anyString(), anyString());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendGradeNotification(gradeNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("成绩通知发送成功");
            assertThat(responseBody.get("experimentId")).isEqualTo("exp123");
            assertThat(responseBody.get("receiverId")).isEqualTo("student1");

            verify(messageService).sendGradeNotification(
                    "实验成绩通知",
                    "您的实验成绩为85分",
                    "exp123",
                    "student1"
            );
        }

        @Test
        @DisplayName("发送成绩通知时服务异常")
        void shouldHandleServiceExceptionForGradeNotification() {
            // given
            doThrow(new RuntimeException("学生不存在"))
                    .when(messageService).sendGradeNotification(
                            anyString(), anyString(), anyString(), anyString());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendGradeNotification(gradeNotificationRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(false);
            assertThat(responseBody.get("message")).isEqualTo("发送成绩通知失败: 学生不存在");

            verify(messageService).sendGradeNotification(anyString(), anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("验证成绩通知参数正确传递")
        void shouldPassCorrectParametersToGradeNotification() {
            // given
            NotificationController.GradeNotificationRequest customRequest = 
                    new NotificationController.GradeNotificationRequest();
            customRequest.setTitle("期末成绩");
            customRequest.setContent("您的期末成绩为优秀");
            customRequest.setExperimentId("final_exam");
            customRequest.setReceiverId("student999");

            doNothing().when(messageService).sendGradeNotification(
                    anyString(), anyString(), anyString(), anyString());

            // when
            ResponseEntity<Map<String, Object>> response = 
                    notificationController.sendGradeNotification(customRequest);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("experimentId")).isEqualTo("final_exam");
            assertThat(responseBody.get("receiverId")).isEqualTo("student999");

            verify(messageService).sendGradeNotification(
                    "期末成绩",
                    "您的期末成绩为优秀",
                    "final_exam",
                    "student999"
            );
        }
    }
}
