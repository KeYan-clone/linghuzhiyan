package org.linghu.message.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 消息通知领域模型
 */
@Entity
@Table(name = "messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Message {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 消息标题
     */
    @Column(name = "title", nullable = false)
    @NotBlank(message = "消息标题不能为空")
    private String title;

    /**
     * 消息内容
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    @NotBlank(message = "消息内容不能为空")
    private String content;

    /**
     * 发送者ID
     */
    @Column(name = "sender_id", nullable = false, length = 50)
    @NotBlank(message = "发送者ID不能为空")
    private String senderId;

    /**
     * 接收者ID
     */
    @Column(name = "receiver_id", nullable = false, length = 50)
    @NotBlank(message = "接收者ID不能为空")
    private String receiverId;

    /**
     * 消息类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    @NotNull
    @Builder.Default
    private MessageType messageType = MessageType.NOTIFICATION;

    /**
     * 消息状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    @NotNull
    @Builder.Default
    private MessageStatus status = MessageStatus.UNREAD;

    /**
     * 优先级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 10)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    /**
     * 相关实验ID（可选）
     */
    @Column(name = "experiment_id", length = 50)
    private String experimentId;

    /**
     * 相关任务ID（可选）
     */
    @Column(name = "task_id", length = 50)
    private String taskId;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 阅读时间
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        NOTIFICATION,    // 系统通知
        EXPERIMENT,      // 实验通知
        GRADE,          // 成绩通知
        DISCUSSION,     // 讨论回复
        ANNOUNCEMENT,   // 公告
        REMINDER        // 提醒
    }

    /**
     * 消息状态枚举
     */
    public enum MessageStatus {
        UNREAD,         // 未读
        READ            // 已读
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        LOW,            // 低
        NORMAL,         // 普通
        HIGH,           // 高
        URGENT          // 紧急
    }
}
