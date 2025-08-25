package org.linghu.resource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO服务器地址
     */
    private String endpoint;

    /**
     * 访问密钥
     */
    private String accessKey;

    /**
     * 秘密密钥
     */
    private String secretKey;

    /**
     * 默认存储桶
     */
    private String defaultBucket = "linghuzhiyan";

    /**
     * 资源存储桶
     */
    private String resourceBucket = "resource";

    /**
     * 提交存储桶
     */
    private String submissionBucket = "submission";
}
