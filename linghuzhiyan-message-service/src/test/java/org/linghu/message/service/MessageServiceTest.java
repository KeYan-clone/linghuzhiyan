package org.linghu.message.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.message.client.UserServiceClient;
import org.linghu.message.config.TestSecurityConfig;
import org.linghu.message.domain.Message;
import org.linghu.message.dto.*;
import org.linghu.message.repository.MessageRepository;
import org.linghu.message.service.impl.MessageServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * MessageService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
@DisplayName("消息服务测试")
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Message sampleMessage;
    private MessageRequestDTO messageRequestDTO;
    private UserServiceClient.UserInfo senderInfo;
    private UserServiceClient.UserInfo receiverInfo;

    @BeforeEach
    void setUp() {
        // 创建测试用户信息
        senderInfo = new UserServiceClient.UserInfo();
        senderInfo.setId("sender123");
        senderInfo.setUsername("testSender");
        senderInfo.setRole("TEACHER");
        senderInfo.setAvatar("avatar1.jpg");

        receiverInfo = new UserServiceClient.UserInfo();
        receiverInfo.setId("receiver456");
        receiverInfo.setUsername("testReceiver");
        receiverInfo.setRole("STUDENT");
        receiverInfo.setAvatar("avatar2.jpg");

        // 创建测试消息
        sampleMessage = Message.builder()
                .id("msg123")
                .title("测试消息标题")
                .content("测试消息内容")
                .senderId("sender123")
                .receiverId("receiver456")
                .messageType(Message.MessageType.NOTIFICATION)
                .status(Message.MessageStatus.UNREAD)
                .priority(Message.Priority.NORMAL)
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

    @Nested
    @DisplayName("创建消息测试")
    class CreateMessageTests {

        @Test
        @DisplayName("成功创建消息")
        void shouldCreateMessageSuccessfully() {
            // given
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);
            when(messageRepository.save(any(Message.class))).thenReturn(sampleMessage);

            // when
            MessageDTO result = messageService.createMessage(messageRequestDTO, "sender123");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("测试消息标题");
            assertThat(result.getContent()).isEqualTo("测试消息内容");
            assertThat(result.getSenderId()).isEqualTo("sender123");
            assertThat(result.getReceiverId()).isEqualTo("receiver456");
            assertThat(result.getMessageType()).isEqualTo("NOTIFICATION");
            assertThat(result.getStatus()).isEqualTo("UNREAD");

            verify(userServiceClient, times(2)).getUserById("receiver456");
            verify(messageRepository).save(any(Message.class));
        }

        @Test
        @DisplayName("接收者不存在时创建消息失败")
        void shouldFailWhenReceiverNotFound() {
            // given
            when(userServiceClient.getUserById("receiver456")).thenReturn(null);

            // when & then
            assertThatThrownBy(() -> messageService.createMessage(messageRequestDTO, "sender123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("接收者不存在");

            verify(userServiceClient).getUserById("receiver456");
            verify(messageRepository, never()).save(any(Message.class));
        }

        @Test
        @DisplayName("用户服务调用异常时创建消息失败")
        void shouldFailWhenUserServiceThrowsException() {
            // given
            when(userServiceClient.getUserById("receiver456"))
                    .thenThrow(new RuntimeException("用户服务不可用"));

            // when & then
            assertThatThrownBy(() -> messageService.createMessage(messageRequestDTO, "sender123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("创建消息失败");

            verify(userServiceClient).getUserById("receiver456");
            verify(messageRepository, never()).save(any(Message.class));
        }
    }

    @Nested
    @DisplayName("获取消息测试")
    class GetMessageTests {

        @Test
        @DisplayName("根据ID成功获取消息")
        void shouldGetMessageByIdSuccessfully() {
            // given
            when(messageRepository.findById("msg123")).thenReturn(Optional.of(sampleMessage));
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            MessageDTO result = messageService.getMessageById("msg123");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("msg123");
            assertThat(result.getTitle()).isEqualTo("测试消息标题");
            assertThat(result.getSenderUsername()).isEqualTo("testSender");
            assertThat(result.getReceiverUsername()).isEqualTo("testReceiver");

            verify(messageRepository).findById("msg123");
        }

        @Test
        @DisplayName("消息不存在时获取失败")
        void shouldFailWhenMessageNotFound() {
            // given
            when(messageRepository.findById("nonexistent")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.getMessageById("nonexistent"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("消息不存在");

            verify(messageRepository).findById("nonexistent");
        }

        @Test
        @DisplayName("成功获取接收者消息列表")
        void shouldGetMessagesByReceiverSuccessfully() {
            // given
            List<Message> messages = Arrays.asList(sampleMessage);
            when(messageRepository.findByReceiverIdOrderByUpdatedAtDesc("receiver456"))
                    .thenReturn(messages);
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            List<MessageDTO> result = messageService.getMessagesByReceiver("receiver456");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReceiverId()).isEqualTo("receiver456");

            verify(messageRepository).findByReceiverIdOrderByUpdatedAtDesc("receiver456");
        }

        @Test
        @DisplayName("成功获取发送者消息列表")
        void shouldGetMessagesBySenderSuccessfully() {
            // given
            List<Message> messages = Arrays.asList(sampleMessage);
            when(messageRepository.findBySenderIdOrderByUpdatedAtDesc("sender123"))
                    .thenReturn(messages);
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            List<MessageDTO> result = messageService.getMessagesBySender("sender123");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getSenderId()).isEqualTo("sender123");

            verify(messageRepository).findBySenderIdOrderByUpdatedAtDesc("sender123");
        }
    }

    @Nested
    @DisplayName("消息状态管理测试")
    class MessageStatusTests {

        @Test
        @DisplayName("成功标记消息为已读")
        void shouldMarkMessageAsReadSuccessfully() {
            // given
            Message unreadMessage = Message.builder()
                    .id("msg123")
                    .title("测试消息")
                    .content("内容")
                    .senderId("sender123")
                    .receiverId("receiver456")
                    .status(Message.MessageStatus.UNREAD)
                    .build();

            Message readMessage = Message.builder()
                    .id("msg123")
                    .title("测试消息")
                    .content("内容")
                    .senderId("sender123")
                    .receiverId("receiver456")
                    .status(Message.MessageStatus.READ)
                    .readAt(LocalDateTime.now())
                    .build();

            when(messageRepository.findById("msg123")).thenReturn(Optional.of(unreadMessage));
            when(messageRepository.save(any(Message.class))).thenReturn(readMessage);
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            MessageDTO result = messageService.markAsRead("msg123");

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("READ");

            verify(messageRepository).findById("msg123");
            verify(messageRepository).save(argThat(msg -> 
                msg.getStatus() == Message.MessageStatus.READ && msg.getReadAt() != null));
        }

        @Test
        @DisplayName("消息不存在时标记已读失败")
        void shouldFailMarkAsReadWhenMessageNotFound() {
            // given
            when(messageRepository.findById("nonexistent")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.markAsRead("nonexistent"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("消息不存在");

            verify(messageRepository).findById("nonexistent");
            verify(messageRepository, never()).save(any(Message.class));
        }

        @Test
        @DisplayName("成功批量标记消息为已读")
        void shouldBatchMarkAsReadSuccessfully() {
            // given
            List<String> messageIds = Arrays.asList("msg1", "msg2", "msg3");
            List<Message> messages = Arrays.asList(
                    Message.builder().id("msg1").receiverId("user123").build(),
                    Message.builder().id("msg2").receiverId("user123").build(),
                    Message.builder().id("msg3").receiverId("user456").build() // 不同用户
            );

            when(messageRepository.findAllById(messageIds)).thenReturn(messages);

            // when
            messageService.batchMarkAsRead(messageIds, "user123");

            // then
            verify(messageRepository).findAllById(messageIds);
            verify(messageRepository).batchUpdateStatus(
                    argThat(ids -> ids.size() == 2 && ids.contains("msg1") && ids.contains("msg2")),
                    eq(Message.MessageStatus.READ),
                    any(LocalDateTime.class)
            );
        }
    }

    @Nested
    @DisplayName("消息删除测试")
    class DeleteMessageTests {

        @Test
        @DisplayName("接收者成功删除消息")
        void shouldDeleteMessageAsReceiver() {
            // given
            when(messageRepository.findById("msg123")).thenReturn(Optional.of(sampleMessage));

            // when
            messageService.deleteMessage("msg123", "receiver456");

            // then
            verify(messageRepository).findById("msg123");
            verify(messageRepository).delete(sampleMessage);
        }

        @Test
        @DisplayName("发送者成功删除消息")
        void shouldDeleteMessageAsSender() {
            // given
            when(messageRepository.findById("msg123")).thenReturn(Optional.of(sampleMessage));

            // when
            messageService.deleteMessage("msg123", "sender123");

            // then
            verify(messageRepository).findById("msg123");
            verify(messageRepository).delete(sampleMessage);
        }

        @Test
        @DisplayName("无权限用户删除消息失败")
        void shouldFailDeleteMessageWhenNoPermission() {
            // given
            when(messageRepository.findById("msg123")).thenReturn(Optional.of(sampleMessage));

            // when & then
            assertThatThrownBy(() -> messageService.deleteMessage("msg123", "unauthorized"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("无权限删除此消息");

            verify(messageRepository).findById("msg123");
            verify(messageRepository, never()).delete(any(Message.class));
        }

        @Test
        @DisplayName("消息不存在时删除失败")
        void shouldFailDeleteWhenMessageNotFound() {
            // given
            when(messageRepository.findById("nonexistent")).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> messageService.deleteMessage("nonexistent", "user123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("消息不存在");

            verify(messageRepository).findById("nonexistent");
            verify(messageRepository, never()).delete(any(Message.class));
        }
    }

    @Nested
    @DisplayName("通知发送测试")
    class NotificationTests {

        @Test
        @DisplayName("成功发送系统通知")
        void shouldSendSystemNotificationSuccessfully() {
            // given
            String title = "系统通知";
            String content = "系统维护通知";
            List<String> receiverIds = Arrays.asList("user1", "user2", "user3");

            when(messageRepository.save(any(Message.class))).thenReturn(sampleMessage);

            // when
            messageService.sendSystemNotification(title, content, receiverIds);

            // then
            verify(messageRepository, times(3)).save(argThat(msg ->
                    msg.getTitle().equals(title) &&
                    msg.getContent().equals(content) &&
                    msg.getSenderId().equals("SYSTEM") &&
                    msg.getMessageType() == Message.MessageType.NOTIFICATION
            ));
        }

        @Test
        @DisplayName("成功发送实验通知")
        void shouldSendExperimentNotificationSuccessfully() {
            // given
            String title = "实验通知";
            String content = "新实验发布";
            String experimentId = "exp123";
            List<String> receiverIds = Arrays.asList("student1", "student2");

            when(messageRepository.save(any(Message.class))).thenReturn(sampleMessage);

            // when
            messageService.sendExperimentNotification(title, content, experimentId, receiverIds);

            // then
            verify(messageRepository, times(2)).save(argThat(msg ->
                    msg.getTitle().equals(title) &&
                    msg.getContent().equals(content) &&
                    msg.getExperimentId().equals(experimentId) &&
                    msg.getMessageType() == Message.MessageType.EXPERIMENT &&
                    msg.getPriority() == Message.Priority.HIGH
            ));
        }

        @Test
        @DisplayName("成功发送成绩通知")
        void shouldSendGradeNotificationSuccessfully() {
            // given
            String title = "成绩通知";
            String content = "实验成绩已发布";
            String experimentId = "exp123";
            String receiverId = "student1";

            when(messageRepository.save(any(Message.class))).thenReturn(sampleMessage);

            // when
            messageService.sendGradeNotification(title, content, experimentId, receiverId);

            // then
            verify(messageRepository).save(argThat(msg ->
                    msg.getTitle().equals(title) &&
                    msg.getContent().equals(content) &&
                    msg.getExperimentId().equals(experimentId) &&
                    msg.getReceiverId().equals(receiverId) &&
                    msg.getMessageType() == Message.MessageType.GRADE &&
                    msg.getPriority() == Message.Priority.HIGH
            ));
        }

        @Test
        @DisplayName("发送成绩通知时保存失败")
        void shouldFailGradeNotificationWhenSaveFails() {
            // given
            String title = "成绩通知";
            String content = "实验成绩已发布";
            String experimentId = "exp123";
            String receiverId = "student1";

            when(messageRepository.save(any(Message.class)))
                    .thenThrow(new RuntimeException("数据库保存失败"));

            // when & then
            assertThatThrownBy(() -> 
                    messageService.sendGradeNotification(title, content, experimentId, receiverId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("发送成绩通知失败");

            verify(messageRepository).save(any(Message.class));
        }
    }

    @Nested
    @DisplayName("消息查询测试")
    class MessageQueryTests {

        @Test
        @DisplayName("成功获取未读消息数量")
        void shouldGetUnreadCountSuccessfully() {
            // given
            when(messageRepository.countByReceiverIdAndStatus("receiver456", Message.MessageStatus.UNREAD))
                    .thenReturn(5L);

            // when
            long result = messageService.getUnreadCount("receiver456");

            // then
            assertThat(result).isEqualTo(5L);

            verify(messageRepository).countByReceiverIdAndStatus("receiver456", Message.MessageStatus.UNREAD);
        }

        @Test
        @DisplayName("成功获取最近消息")
        void shouldGetRecentMessagesSuccessfully() {
            // given
            List<Message> messages = Arrays.asList(sampleMessage);
            when(messageRepository.findRecentMessages(any(Pageable.class))).thenReturn(messages);
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            List<MessageDTO> result = messageService.getRecentMessages("receiver456", 10);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getReceiverId()).isEqualTo("receiver456");

            verify(messageRepository).findRecentMessages(any(Pageable.class));
        }

        @Test
        @DisplayName("成功搜索消息")
        void shouldSearchMessagesSuccessfully() {
            // given
            String keyword = "测试";
            List<Message> titleResults = Arrays.asList(sampleMessage);
            List<Message> contentResults = new ArrayList<>();

            when(messageRepository.findByTitleContaining(keyword)).thenReturn(titleResults);
            when(messageRepository.findByContentContaining(keyword)).thenReturn(contentResults);
            when(userServiceClient.getUserById("sender123")).thenReturn(senderInfo);
            when(userServiceClient.getUserById("receiver456")).thenReturn(receiverInfo);

            // when
            List<MessageDTO> result = messageService.searchMessages(keyword, "receiver456");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getTitle()).contains("测试");

            verify(messageRepository).findByTitleContaining(keyword);
            verify(messageRepository).findByContentContaining(keyword);
        }
    }
}
