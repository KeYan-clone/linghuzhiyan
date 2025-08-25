package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;

/**
 * 审核请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDTO {

    @NotEmpty(message = "审核状态不能为空")
    private String status; // APPROVED 或 REJECTED

    private String rejectionReason; // 拒绝原因（拒绝时必填）
}
