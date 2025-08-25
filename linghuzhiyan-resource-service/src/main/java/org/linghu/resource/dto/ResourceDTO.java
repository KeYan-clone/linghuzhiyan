package org.linghu.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * 资源DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceDTO {

    private String id;

    private String experimentId;

    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    @NotBlank(message = "资源路径不能为空")
    private String resourcePath;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    private Long fileSize;

    private String mimeType;

    private String description;

    private String uploader;

    private Boolean isPublic;

    private Integer downloadCount;

    private LocalDateTime uploadTime;

    private LocalDateTime updatedAt;

    // 辅助字段，用于前端显示
    private String fileSizeFormatted;
    private String downloadUrl;
}
