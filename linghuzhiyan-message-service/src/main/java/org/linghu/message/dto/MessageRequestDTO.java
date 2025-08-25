package org.linghu.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 消息请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequestDTO {

    @NotBlank(message = "消息标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    @Size(max = 2000, message = "内容长度不能超过2000个字符")
    private String content;

    @NotBlank(message = "接收者ID不能为空")
    private String receiverId;

    /**
     * 消息类型：NOTIFICATION, EXPERIMENT, GRADE, DISCUSSION, ANNOUNCEMENT, REMINDER
     */
    @Builder.Default
    private String messageType = "NOTIFICATION";

    /**
     * 优先级：LOW, NORMAL, HIGH, URGENT
     */
    @Builder.Default
    private String priority = "NORMAL";

    /**
     * 相关实验ID（可选）
     */
    private String experimentId;

    /**
     * 相关任务ID（可选）
     */
    private String taskId;
}
