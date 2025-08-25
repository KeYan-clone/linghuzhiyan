package org.linghu.message.service;

import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageQueryDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.dto.SenderInfoDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 创建消息
     */
    MessageDTO createMessage(MessageRequestDTO requestDTO, String senderId);

    /**
     * 根据ID获取消息
     */
    MessageDTO getMessageById(String id);

    /**
     * 获取接收者的消息列表
     */
    List<MessageDTO> getMessagesByReceiver(String receiverId);

    /**
     * 获取发送者的消息列表
     */
    List<MessageDTO> getMessagesBySender(String senderId);

    /**
     * 获取指定发送者发给指定接收者的消息
     */
    List<MessageDTO> getMessagesBySenderAndReceiver(String senderId, String receiverId);

    /**
     * 获取所有消息（管理员功能）
     */
    List<MessageDTO> getAllMessages();

    /**
     * 标记消息为已读
     */
    MessageDTO markAsRead(String id);

    /**
     * 批量标记消息为已读
     */
    void batchMarkAsRead(List<String> ids, String currentUser);

    /**
     * 删除消息
     */
    void deleteMessage(String id, String currentUser);

    /**
     * 获取发送者信息列表
     */
    List<SenderInfoDTO> getSendersByReceiver(String receiverId);

    /**
     * 分页查询消息
     */
    Page<MessageDTO> queryMessages(MessageQueryDTO queryDTO);

    /**
     * 搜索消息
     */
    List<MessageDTO> searchMessages(String keyword, String currentUser);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(String receiverId);

    /**
     * 获取最近消息
     */
    List<MessageDTO> getRecentMessages(String receiverId, int limit);

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
