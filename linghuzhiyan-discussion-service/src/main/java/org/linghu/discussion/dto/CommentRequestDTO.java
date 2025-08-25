package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建评论请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequestDTO {

    @NotEmpty(message = "评论内容不能为空")
    @Size(max = 5000, message = "评论内容长度不能超过5000个字符")
    private String content;

    private String parentId;

    private String replyToUserId;
}
