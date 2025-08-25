package org.linghu.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Size;

/**
 * 资源请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceRequestDTO {

    /**
     * 实验ID（可选）
     */
    private String experimentId;

    /**
     * 资源描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    /**
     * 是否公开
     */
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * 上传类型：experiment（实验资源）、resource（学习资料）
     */
    private String uploadType;

    /**
     * 是否自动解压（仅对压缩包有效）
     */
    @Builder.Default
    private Boolean autoExtract = false;
}
