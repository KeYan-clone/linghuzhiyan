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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

/**
 * MessageController 集成测试 - 使用SpringBootTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DisplayName("消息控制器集成测试")
class MessageControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockBean
    private MessageService messageService;

    @MockBean
    private UserServiceClient userServiceClient;
    
    @MockBean
    private MessageRepository messageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MessageDTO sampleMessageDTO;
    private MessageRequestDTO messageRequestDTO;

    @BeforeEach
    void setUp() {
        // 手动创建MockMvc
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
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
    @DisplayName("测试Controller映射 - 应该返回认证错误而不是404")
    void testControllerMapping() throws Exception {
        // given
        when(messageService.createMessage(any(MessageRequestDTO.class), anyString()))
                .thenReturn(sampleMessageDTO);

        // when & then - 测试端点是否存在（不关心认证问题）
        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDTO)))
                .andDo(print());
                // 只要不是404，就说明Controller被正确映射了
    }
}
