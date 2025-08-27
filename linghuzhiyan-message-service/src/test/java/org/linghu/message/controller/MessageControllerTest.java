package org.linghu.message.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.dto.SenderInfoDTO;
import org.linghu.message.service.MessageService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageController 单元测试 - 使用纯单元测试方式
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息控制器测试")
class MessageControllerTest {

    @Mock
    private MessageService messageService;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private MessageController messageController;

    private MessageDTO sampleMessageDTO;
    private MessageRequestDTO messageRequestDTO;
    private List<MessageDTO> messageList;
    private List<SenderInfoDTO> senderList;

    @BeforeEach
    void setUp() {
        // 创建测试消息DTO
        sampleMessageDTO = MessageDTO.builder()
                .id("msg123")
                .title("测试消息标题")
                .content("测试消息内容")
                .senderId("sender123")
                .receiverId("receiver456")
                .senderUsername("testSender")
                .receiverUsername("testReceiver")
                .messageType("NOTIFICATION")
                .status("UNREAD")
                .priority("NORMAL")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 创建测试消息请求
        messageRequestDTO = new MessageRequestDTO();
        messageRequestDTO.setTitle("测试消息标题");
        messageRequestDTO.setContent("测试消息内容");
        messageRequestDTO.setReceiverId("receiver456");
        messageRequestDTO.setMessageType("NOTIFICATION");
        messageRequestDTO.setPriority("NORMAL");

        // 创建消息列表
        messageList = Arrays.asList(sampleMessageDTO);
        
        // 创建发送者列表
        SenderInfoDTO senderInfo = SenderInfoDTO.builder()
                .senderId("sender123")
                .senderUsername("testSender")
                .unreadCount(1L)
                .build();
        senderList = Arrays.asList(senderInfo);
    }

    private void setupSecurityContext() {
        // 设置Security上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("创建消息测试")
    class CreateMessageTests {

        @Test
        @DisplayName("成功创建消息")
        void shouldCreateMessageSuccessfully() {
            // given
            setupSecurityContext();
            when(messageService.createMessage(any(MessageRequestDTO.class), eq("user123")))
                    .thenReturn(sampleMessageDTO);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.createMessage(messageRequestDTO);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("消息创建成功");
            assertThat(responseBody.get("data")).isEqualTo(sampleMessageDTO);

            verify(messageService).createMessage(any(MessageRequestDTO.class), eq("user123"));
        }

        @Test
        @DisplayName("创建消息时服务异常")
        void shouldHandleServiceException() {
            // given
            setupSecurityContext();
            when(messageService.createMessage(any(MessageRequestDTO.class), eq("user123")))
                    .thenThrow(new RuntimeException("服务异常"));

            // when
            ResponseEntity<Map<String, Object>> response = messageController.createMessage(messageRequestDTO);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(false);
            assertThat(responseBody.get("message")).asString().contains("创建消息失败");
        }
    }

    @Nested
    @DisplayName("获取消息测试")
    class GetMessageTests {

        @Test
        @DisplayName("根据ID获取消息成功")
        void shouldGetMessageByIdSuccessfully() {
            // given
            when(messageService.getMessageById("msg123")).thenReturn(sampleMessageDTO);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.getMessageById("msg123");

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(sampleMessageDTO);

            verify(messageService).getMessageById("msg123");
        }

        @Test
        @DisplayName("获取接收的消息")
        void shouldGetReceivedMessages() {
            // given
            setupSecurityContext();
            when(messageService.getMessagesByReceiver("user123")).thenReturn(messageList);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.getReceivedMessages();

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(messageList);
            assertThat(responseBody.get("total")).isEqualTo(1);

            verify(messageService).getMessagesByReceiver("user123");
        }

        @Test
        @DisplayName("获取发送的消息")
        void shouldGetSentMessages() {
            // given
            setupSecurityContext();
            when(messageService.getMessagesBySender("user123")).thenReturn(messageList);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.getSentMessages();

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(messageList);

            verify(messageService).getMessagesBySender("user123");
        }
    }

    @Nested
    @DisplayName("消息状态管理测试")
    class MessageStatusTests {

        @Test
        @DisplayName("标记消息为已读")
        void shouldMarkMessageAsRead() {
            // given
            MessageDTO readMessage = MessageDTO.builder()
                    .id("msg123")
                    .status("READ")
                    .build();
            when(messageService.markAsRead("msg123")).thenReturn(readMessage);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.markAsRead("msg123");

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("消息已标记为已读");

            verify(messageService).markAsRead("msg123");
        }

        @Test
        @DisplayName("批量标记消息为已读")
        void shouldBatchMarkAsRead() {
            // given
            setupSecurityContext();
            List<String> ids = Arrays.asList("msg1", "msg2");

            // when
            ResponseEntity<Map<String, Object>> response = messageController.batchMarkAsRead(ids);

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("批量标记消息为已读成功");

            verify(messageService).batchMarkAsRead(ids, "user123");
        }

        @Test
        @DisplayName("删除消息")
        void shouldDeleteMessage() {
            // given
            setupSecurityContext();
            
            // when
            ResponseEntity<Map<String, Object>> response = messageController.deleteMessage("msg123");

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("message")).isEqualTo("消息删除成功");

            verify(messageService).deleteMessage("msg123", "user123");
        }
    }

    @Nested
    @DisplayName("其他功能测试")
    class OtherFunctionTests {

        @Test
        @DisplayName("获取发送者信息")
        void shouldGetSenders() {
            // given
            setupSecurityContext();
            when(messageService.getSendersByReceiver("user123")).thenReturn(senderList);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.getSenders();

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(senderList);

            verify(messageService).getSendersByReceiver("user123");
        }

        @Test
        @DisplayName("搜索消息")
        void shouldSearchMessages() {
            // given
            setupSecurityContext();
            when(messageService.searchMessages("keyword", "user123")).thenReturn(messageList);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.searchMessages("keyword");

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(messageList);

            verify(messageService).searchMessages("keyword", "user123");
        }

        @Test
        @DisplayName("获取未读消息数量")
        void shouldGetUnreadCount() {
            // given
            setupSecurityContext();
            when(messageService.getUnreadCount("user123")).thenReturn(5L);

            // when
            ResponseEntity<Map<String, Object>> response = messageController.getUnreadCount();

            // then
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).isNotNull();
            
            Map<String, Object> responseBody = response.getBody();
            assertThat(responseBody.get("success")).isEqualTo(true);
            assertThat(responseBody.get("data")).isEqualTo(5L);

            verify(messageService).getUnreadCount("user123");
        }
    }
}
