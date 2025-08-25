package org.linghu.message.repository;

import org.linghu.message.domain.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息仓储接口
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    /**
     * 根据接收者ID查找消息，按更新时间倒序
     */
    List<Message> findByReceiverIdOrderByUpdatedAtDesc(String receiverId);

    /**
     * 根据发送者ID查找消息，按更新时间倒序
     */
    List<Message> findBySenderIdOrderByUpdatedAtDesc(String senderId);

    /**
     * 根据发送者和接收者ID查找消息，按更新时间倒序
     */
    List<Message> findBySenderIdAndReceiverIdOrderByUpdatedAtDesc(String senderId, String receiverId);

    /**
     * 查找所有消息，按更新时间倒序
     */
    List<Message> findAllByOrderByUpdatedAtDesc();

    /**
     * 根据接收者ID查找消息
     */
    List<Message> findByReceiverId(String receiverId);

    /**
     * 根据接收者ID和状态查找消息
     */
    List<Message> findByReceiverIdAndStatus(String receiverId, Message.MessageStatus status);

    /**
     * 根据接收者ID和消息类型查找消息
     */
    List<Message> findByReceiverIdAndMessageType(String receiverId, Message.MessageType messageType);

    /**
     * 根据实验ID查找消息
     */
    List<Message> findByExperimentId(String experimentId);

    /**
     * 根据任务ID查找消息
     */
    List<Message> findByTaskId(String taskId);

    /**
     * 统计接收者未读消息数量
     */
    long countByReceiverIdAndStatus(String receiverId, Message.MessageStatus status);

    /**
     * 统计发送者发送的消息数量
     */
    long countBySenderId(String senderId);

    /**
     * 分页查询接收者的消息
     */
    Page<Message> findByReceiverIdOrderByCreatedAtDesc(String receiverId, Pageable pageable);

    /**
     * 分页查询发送者的消息
     */
    Page<Message> findBySenderIdOrderByCreatedAtDesc(String senderId, Pageable pageable);

    /**
     * 查找指定时间之后的消息
     */
    List<Message> findByCreatedAtAfter(LocalDateTime dateTime);

    /**
     * 查找指定优先级的消息
     */
    List<Message> findByPriority(Message.Priority priority);

    /**
     * 根据标题模糊查询
     */
    @Query("SELECT m FROM Message m WHERE m.title LIKE %:title%")
    List<Message> findByTitleContaining(@Param("title") String title);

    /**
     * 根据内容模糊查询
     */
    @Query("SELECT m FROM Message m WHERE m.content LIKE %:content%")
    List<Message> findByContentContaining(@Param("content") String content);

    /**
     * 获取接收者的发送者列表（去重）
     */
    @Query("SELECT DISTINCT m.senderId FROM Message m WHERE m.receiverId = :receiverId")
    List<String> findDistinctSenderIdsByReceiverId(@Param("receiverId") String receiverId);

    /**
     * 查找最近的消息
     */
    @Query("SELECT m FROM Message m ORDER BY m.createdAt DESC")
    List<Message> findRecentMessages(Pageable pageable);

    /**
     * 根据多个条件查询消息
     */
    @Query("SELECT m FROM Message m WHERE " +
           "(:receiverId IS NULL OR m.receiverId = :receiverId) AND " +
           "(:senderId IS NULL OR m.senderId = :senderId) AND " +
           "(:messageType IS NULL OR m.messageType = :messageType) AND " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:experimentId IS NULL OR m.experimentId = :experimentId) " +
           "ORDER BY m.createdAt DESC")
    Page<Message> findByConditions(@Param("receiverId") String receiverId,
                                   @Param("senderId") String senderId,
                                   @Param("messageType") Message.MessageType messageType,
                                   @Param("status") Message.MessageStatus status,
                                   @Param("experimentId") String experimentId,
                                   Pageable pageable);

    /**
     * 批量标记消息为已读
     */
    @Query("UPDATE Message m SET m.status = :status, m.readAt = :readAt WHERE m.id IN :ids")
    void batchUpdateStatus(@Param("ids") List<String> ids, 
                          @Param("status") Message.MessageStatus status,
                          @Param("readAt") LocalDateTime readAt);
}
