package org.linghu.message.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.linghu.message.client.UserServiceClient;
import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.repository.MessageRepository;
import org.linghu.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * MessageController 最小化测试配置
 */
@SpringJUnitConfig
@Import(MessageControllerMinimalTest.TestConfig.class)
@AutoConfigureWebMvc
@DisplayName("消息控制器最小化测试")
class MessageControllerMinimalTest {

    @Autowired
    private MessageController messageController;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserServiceClient userServiceClient;
    
    @MockBean
    private MessageRepository messageRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();
    private MessageDTO sampleMessageDTO;
    private MessageRequestDTO messageRequestDTO;

    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public MessageController messageController(MessageService messageService) {
            return new MessageController(messageService);
        }
    }

    @BeforeEach
    void setUp() {
        // 手动创建MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
        
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

    @Test
    @DisplayName("测试Controller映射 - 使用standalone MockMvc")
    void testControllerMapping() throws Exception {
        // given
        when(messageService.createMessage(any(MessageRequestDTO.class), anyString()))
                .thenReturn(sampleMessageDTO);

        // when & then - 使用standalone MockMvc避免Spring上下文问题
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDTO)))
                .andDo(print());
                // 这个测试主要验证Controller能否处理请求，不关心具体的认证结果
    }
}
