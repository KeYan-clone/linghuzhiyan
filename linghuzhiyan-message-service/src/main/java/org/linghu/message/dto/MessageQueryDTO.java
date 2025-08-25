package org.linghu.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息查询DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageQueryDTO {

    /**
     * 接收者ID
     */
    private String receiverId;

    /**
     * 发送者ID
     */
    private String senderId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 消息状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 标题关键词
     */
    private String titleKeyword;

    /**
     * 内容关键词
     */
    private String contentKeyword;

    /**
     * 页码（从0开始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每页大小
     */
    @Builder.Default
    private Integer size = 10;

    /**
     * 排序字段
     */
    @Builder.Default
    private String sortBy = "createdAt";

    /**
     * 排序方向：asc、desc
     */
    @Builder.Default
    private String sortDirection = "desc";
}
