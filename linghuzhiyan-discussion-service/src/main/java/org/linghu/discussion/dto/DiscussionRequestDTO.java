package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 创建讨论请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionRequestDTO {

    @NotEmpty(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;

    @NotEmpty(message = "内容不能为空")
    @Size(max = 10000, message = "内容长度不能超过10000个字符")
    private String content;

    private List<String> tags;

    private String experimentId;
}
