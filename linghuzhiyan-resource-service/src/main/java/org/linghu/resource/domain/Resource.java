package org.linghu.resource.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 资源实体类
 */
@Entity
@Table(name = "resources")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Resource {

    @Id
    @Column(name = "id", length = 50)
    private String id;

    /**
     * 实验ID（可选，如果是实验相关资源）
     */
    @Column(name = "experiment_id", length = 50)
    private String experimentId;

    /**
     * 资源类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false, length = 20)
    @NotNull
    private ResourceType resourceType;

    /**
     * 资源路径（MinIO对象名）
     */
    @Column(name = "resource_path", nullable = false, length = 500)
    @NotBlank
    private String resourcePath;

    /**
     * 文件名
     */
    @Column(name = "file_name", nullable = false)
    @NotBlank
    private String fileName;

    /**
     * 文件大小（字节）
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * MIME类型
     */
    @Column(name = "mime_type", length = 100)
    private String mimeType;

    /**
     * 资源描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 上传者用户名
     */
    @Column(name = "uploader", length = 50)
    private String uploader;

    /**
     * 是否公开
     */
    @Column(name = "is_public")
    @Builder.Default
    private Boolean isPublic = true;

    /**
     * 下载次数
     */
    @Column(name = "download_count")
    @Builder.Default
    private Integer downloadCount = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 资源类型枚举
     */
    public enum ResourceType {
        DOCUMENT,     // 文档
        VIDEO,        // 视频
        AUDIO,        // 音频
        IMAGE,        // 图片
        ARCHIVE,      // 压缩包
        CODE,         // 代码文件
        SUBMISSION,   // 学生提交
        OTHER         // 其他
    }
}
