package org.linghu.message.service;

import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.SenderInfoDTO;

import java.util.List;

/**
 * 消息通知服务接口
 */
public interface MessageService {
    MessageDTO createMessage(MessageDTO messageDTO);
    MessageDTO getMessageById(String id);
    List<MessageDTO> getMessagesByReceiver(String receiver);
    /**
     * 获取指定发送者发给指定接收者的消息
     */
    List<MessageDTO> getMessagesBySenderAndReceiver(String sender, String receiver);

    /**
     * 获取指定发送者以指定权限等级发送的所有消息
     */
    List<MessageDTO> getMessagesBySenderAndRole(String sender, String senderRole);

    /**
     * 获取所有消息（仅管理员可用）
     */
    List<MessageDTO> getAllMessages();
    MessageDTO markAsRead(String id);
    void deleteMessage(String id);

    /**
     * 获取给指定接收者发送消息的所有发送者信息（用户名、id、权限等级）
     */
    List<SenderInfoDTO> getSendersByReceiver(String receiverUsername);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(String title, String content, List<String> receiverIds);

    /**
     * 发送实验通知
     */
    void sendExperimentNotification(String title, String content, String experimentId, List<String> receiverIds);

    /**
     * 发送成绩通知
     */
    void sendGradeNotification(String title, String content, String experimentId, String receiverId);
}
