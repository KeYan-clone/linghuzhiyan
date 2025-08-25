package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

/**
 * 举报请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    @NotEmpty(message = "举报原因不能为空")
    private String reason; // 举报原因

    private String details; // 举报详情
}
