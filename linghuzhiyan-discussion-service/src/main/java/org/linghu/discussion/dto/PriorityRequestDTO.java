package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * 优先级请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityRequestDTO {

    @NotNull(message = "优先级不能为空")
    private Integer priority; // 数值越大优先级越高
}
