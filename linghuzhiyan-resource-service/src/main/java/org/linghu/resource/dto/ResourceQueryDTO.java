package org.linghu.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 资源查询请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceQueryDTO {

    /**
     * 实验ID
     */
    private String experimentId;

    /**
     * 资源类型
     */
    private String resourceType;

    /**
     * 上传者
     */
    private String uploader;

    /**
     * 文件名关键词
     */
    private String fileName;

    /**
     * 描述关键词
     */
    private String description;

    /**
     * 是否只查询公开资源
     */
    @Builder.Default
    private Boolean publicOnly = false;

    /**
     * MIME类型前缀
     */
    private String mimeTypePrefix;

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
