package org.linghu.message.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.message.client.UserServiceClient;
import org.linghu.message.domain.Message;
import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageQueryDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.dto.SenderInfoDTO;
import org.linghu.message.repository.MessageRepository;
import org.linghu.message.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final UserServiceClient userServiceClient;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional
    public MessageDTO createMessage(MessageRequestDTO requestDTO, String senderId) {
        try {
            // 验证接收者是否存在
            UserServiceClient.UserInfo receiver = userServiceClient.getUserById(requestDTO.getReceiverId());
            if (receiver == null) {
                throw new RuntimeException("接收者不存在: " + requestDTO.getReceiverId());
            }

            Message message = Message.builder()
                    .title(requestDTO.getTitle())
                    .content(requestDTO.getContent())
                    .senderId(senderId)
                    .receiverId(requestDTO.getReceiverId())
                    .messageType(Message.MessageType.valueOf(requestDTO.getMessageType().toUpperCase()))
                    .priority(Message.Priority.valueOf(requestDTO.getPriority().toUpperCase()))
                    .status(Message.MessageStatus.UNREAD)
                    .experimentId(requestDTO.getExperimentId())
                    .taskId(requestDTO.getTaskId())
                    .build();

            Message savedMessage = messageRepository.save(message);
            log.info("创建消息成功: id={}, title={}, senderId={}, receiverId={}", 
                    savedMessage.getId(), savedMessage.getTitle(), senderId, requestDTO.getReceiverId());

            return convertToDTO(savedMessage);

        } catch (Exception ex) {
            log.error("创建消息失败: senderId={}, receiverId={}, error={}", 
                     senderId, requestDTO.getReceiverId(), ex.getMessage());
            throw new RuntimeException("创建消息失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public MessageDTO getMessageById(String id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在: " + id));
        return convertToDTO(message);
    }

    @Override
    public List<MessageDTO> getMessagesByReceiver(String receiverId) {
        List<Message> messages = messageRepository.findByReceiverIdOrderByUpdatedAtDesc(receiverId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getMessagesBySender(String senderId) {
        List<Message> messages = messageRepository.findBySenderIdOrderByUpdatedAtDesc(senderId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getMessagesBySenderAndReceiver(String senderId, String receiverId) {
        List<Message> messages = messageRepository.findBySenderIdAndReceiverIdOrderByUpdatedAtDesc(senderId, receiverId);
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageDTO> getAllMessages() {
        List<Message> messages = messageRepository.findAllByOrderByUpdatedAtDesc();
        return messages.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MessageDTO markAsRead(String id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在: " + id));

        message.setStatus(Message.MessageStatus.READ);
        message.setReadAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);

        log.info("标记消息为已读: id={}, receiverId={}", id, message.getReceiverId());
        return convertToDTO(savedMessage);
    }

    @Override
    @Transactional
    public void batchMarkAsRead(List<String> ids, String currentUser) {
        // 验证消息是否属于当前用户
        List<Message> messages = messageRepository.findAllById(ids);
        List<String> validIds = messages.stream()
                .filter(msg -> msg.getReceiverId().equals(currentUser))
                .map(Message::getId)
                .collect(Collectors.toList());

        if (!validIds.isEmpty()) {
            messageRepository.batchUpdateStatus(validIds, Message.MessageStatus.READ, LocalDateTime.now());
            log.info("批量标记消息为已读: userId={}, count={}", currentUser, validIds.size());
        }
    }

    @Override
    @Transactional
    public void deleteMessage(String id, String currentUser) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("消息不存在: " + id));

        // 权限检查：只有接收者或发送者可以删除
        if (!message.getReceiverId().equals(currentUser) && !message.getSenderId().equals(currentUser)) {
            throw new RuntimeException("无权限删除此消息");
        }

        messageRepository.delete(message);
        log.info("删除消息成功: id={}, userId={}", id, currentUser);
    }

    @Override
    public List<SenderInfoDTO> getSendersByReceiver(String receiverId) {
        List<String> senderIds = messageRepository.findDistinctSenderIdsByReceiverId(receiverId);
        
        return senderIds.stream()
                .map(senderId -> {
                    try {
                        UserServiceClient.UserInfo sender = userServiceClient.getUserById(senderId);
                        if (sender != null) {
                            // 统计该发送者发送的消息数和未读数
                            long messageCount = messageRepository.countBySenderId(senderId);
                            long unreadCount = messageRepository.countByReceiverIdAndStatus(receiverId, Message.MessageStatus.UNREAD);

                            return SenderInfoDTO.builder()
                                    .senderId(senderId)
                                    .senderUsername(sender.getUsername())
                                    .senderRole(sender.getRole())
                                    .avatar(sender.getAvatar())
                                    .messageCount(messageCount)
                                    .unreadCount(unreadCount)
                                    .build();
                        }
                    } catch (Exception e) {
                        log.warn("获取发送者信息失败: senderId={}, error={}", senderId, e.getMessage());
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MessageDTO> queryMessages(MessageQueryDTO queryDTO) {
        Pageable pageable = createPageable(queryDTO);
        
        Message.MessageType messageType = null;
        if (StringUtils.hasText(queryDTO.getMessageType())) {
            messageType = Message.MessageType.valueOf(queryDTO.getMessageType().toUpperCase());
        }

        Message.MessageStatus status = null;
        if (StringUtils.hasText(queryDTO.getStatus())) {
            status = Message.MessageStatus.valueOf(queryDTO.getStatus().toUpperCase());
        }

        Page<Message> messages = messageRepository.findByConditions(
                queryDTO.getReceiverId(),
                queryDTO.getSenderId(),
                messageType,
                status,
                queryDTO.getExperimentId(),
                pageable
        );

        return messages.map(this::convertToDTO);
    }

    @Override
    public List<MessageDTO> searchMessages(String keyword, String currentUser) {
        List<Message> titleResults = messageRepository.findByTitleContaining(keyword);
        List<Message> contentResults = messageRepository.findByContentContaining(keyword);

        // 合并结果并去重，只返回当前用户作为接收者的消息
        Set<Message> allResults = new HashSet<>();
        allResults.addAll(titleResults);
        allResults.addAll(contentResults);

        return allResults.stream()
                .filter(msg -> msg.getReceiverId().equals(currentUser))
                .map(this::convertToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String receiverId) {
        return messageRepository.countByReceiverIdAndStatus(receiverId, Message.MessageStatus.UNREAD);
    }

    @Override
    public List<MessageDTO> getRecentMessages(String receiverId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Message> messages = messageRepository.findRecentMessages(pageable);
        
        return messages.stream()
                .filter(msg -> msg.getReceiverId().equals(receiverId))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void sendSystemNotification(String title, String content, List<String> receiverIds) {
        for (String receiverId : receiverIds) {
            try {
                Message message = Message.builder()
                        .title(title)
                        .content(content)
                        .senderId("SYSTEM") // 系统发送者
                        .receiverId(receiverId)
                        .messageType(Message.MessageType.NOTIFICATION)
                        .priority(Message.Priority.NORMAL)
                        .status(Message.MessageStatus.UNREAD)
                        .build();

                messageRepository.save(message);
            } catch (Exception e) {
                log.error("发送系统通知失败: receiverId={}, error={}", receiverId, e.getMessage());
            }
        }
        
        log.info("发送系统通知完成: title={}, receiverCount={}", title, receiverIds.size());
    }

    @Override
    @Transactional
    public void sendExperimentNotification(String title, String content, String experimentId, List<String> receiverIds) {
        for (String receiverId : receiverIds) {
            try {
                Message message = Message.builder()
                        .title(title)
                        .content(content)
                        .senderId("SYSTEM")
                        .receiverId(receiverId)
                        .messageType(Message.MessageType.EXPERIMENT)
                        .priority(Message.Priority.HIGH)
                        .status(Message.MessageStatus.UNREAD)
                        .experimentId(experimentId)
                        .build();

                messageRepository.save(message);
            } catch (Exception e) {
                log.error("发送实验通知失败: receiverId={}, experimentId={}, error={}", 
                         receiverId, experimentId, e.getMessage());
            }
        }
        
        log.info("发送实验通知完成: title={}, experimentId={}, receiverCount={}", 
                title, experimentId, receiverIds.size());
    }

    @Override
    @Transactional
    public void sendGradeNotification(String title, String content, String experimentId, String receiverId) {
        try {
            Message message = Message.builder()
                    .title(title)
                    .content(content)
                    .senderId("SYSTEM")
                    .receiverId(receiverId)
                    .messageType(Message.MessageType.GRADE)
                    .priority(Message.Priority.HIGH)
                    .status(Message.MessageStatus.UNREAD)
                    .experimentId(experimentId)
                    .build();

            messageRepository.save(message);
            log.info("发送成绩通知完成: title={}, experimentId={}, receiverId={}", 
                    title, experimentId, receiverId);
            
        } catch (Exception e) {
            log.error("发送成绩通知失败: receiverId={}, experimentId={}, error={}", 
                     receiverId, experimentId, e.getMessage());
            throw new RuntimeException("发送成绩通知失败: " + e.getMessage(), e);
        }
    }

    /**
     * 转换为DTO
     */
    private MessageDTO convertToDTO(Message message) {
        String senderUsername = "";
        String receiverUsername = "";

        // 获取发送者用户名
        try {
            if (!"SYSTEM".equals(message.getSenderId())) {
                UserServiceClient.UserInfo sender = userServiceClient.getUserById(message.getSenderId());
                if (sender != null) {
                    senderUsername = sender.getUsername();
                }
            } else {
                senderUsername = "系统";
            }
        } catch (Exception e) {
            log.warn("获取发送者用户名失败: senderId={}", message.getSenderId());
            senderUsername = "未知用户";
        }

        // 获取接收者用户名
        try {
            UserServiceClient.UserInfo receiver = userServiceClient.getUserById(message.getReceiverId());
            if (receiver != null) {
                receiverUsername = receiver.getUsername();
            }
        } catch (Exception e) {
            log.warn("获取接收者用户名失败: receiverId={}", message.getReceiverId());
            receiverUsername = "未知用户";
        }

        return MessageDTO.builder()
                .id(message.getId())
                .title(message.getTitle())
                .content(message.getContent())
                .senderId(message.getSenderId())
                .receiverId(message.getReceiverId())
                .senderUsername(senderUsername)
                .receiverUsername(receiverUsername)
                .messageType(message.getMessageType().name())
                .status(message.getStatus().name())
                .priority(message.getPriority().name())
                .experimentId(message.getExperimentId())
                .taskId(message.getTaskId())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .readAt(message.getReadAt())
                .createdAtFormatted(message.getCreatedAt() != null ? message.getCreatedAt().format(FORMATTER) : null)
                .updatedAtFormatted(message.getUpdatedAt() != null ? message.getUpdatedAt().format(FORMATTER) : null)
                .readAtFormatted(message.getReadAt() != null ? message.getReadAt().format(FORMATTER) : null)
                .build();
    }

    /**
     * 创建分页对象
     */
    private Pageable createPageable(MessageQueryDTO queryDTO) {
        Sort.Direction direction = Sort.Direction.fromString(queryDTO.getSortDirection());
        Sort sort = Sort.by(direction, queryDTO.getSortBy());
        return PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
    }
}
