package org.linghu.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 消息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDTO {

    private String id;

    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    private String senderId;

    private String receiverId;

    private String senderUsername;

    private String receiverUsername;

    private String messageType;

    private String status;

    private String priority;

    private String experimentId;

    private String taskId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime readAt;

    // 格式化的时间字符串（用于前端显示）
    private String createdAtFormatted;
    private String updatedAtFormatted;
    private String readAtFormatted;
}
