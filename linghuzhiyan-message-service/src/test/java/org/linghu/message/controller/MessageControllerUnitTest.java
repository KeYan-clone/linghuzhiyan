package org.linghu.message.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.service.MessageService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageController 纯单元测试 - 不依赖Spring上下文
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("消息控制器纯单元测试")
class MessageControllerUnitTest {

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

    @BeforeEach
    void setUp() {
        // 设置Security上下文
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("sender123");
        SecurityContextHolder.setContext(securityContext);
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
    }

    @AfterEach
    void tearDown() {
        // 清理Security上下文
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("测试创建消息方法能否正常工作")
    void testCreateMessageMethod() {
        // given
        when(messageService.createMessage(any(MessageRequestDTO.class), anyString()))
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

        // verify
        verify(messageService).createMessage(any(MessageRequestDTO.class), eq("sender123"));
    }
}
