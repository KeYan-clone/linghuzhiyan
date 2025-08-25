package org.linghu.resource.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO客户端配置
 */
@Configuration
public class MinioClientConfig {

    @Autowired
    private MinioConfig minioConfig;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioConfig.getEndpoint())
                .credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey())
                .build();
    }
}
