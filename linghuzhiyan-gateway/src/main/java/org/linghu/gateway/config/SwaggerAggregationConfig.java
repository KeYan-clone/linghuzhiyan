package org.linghu.gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

/**
 * Swagger聚合配置
 * 
 * @author linghu
 * @version 1.0.0
 */
@Configuration
public class SwaggerAggregationConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfig() {
        SwaggerUiConfigProperties properties = new SwaggerUiConfigProperties();
        
        // 设置Swagger URLs
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls = new HashSet<>();
        
        // 添加各个服务的Swagger文档URL
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("用户服务", "/user-service/v3/api-docs", "user-service"));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("认证服务", "/auth-service/v3/api-docs", "auth-service"));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("实验服务", "/experiment-service/v3/api-docs", "experiment-service"));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("资源服务", "/resource-service/v3/api-docs", "resource-service"));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("消息服务", "/message-service/v3/api-docs", "message-service"));
        urls.add(new AbstractSwaggerUiConfigProperties.SwaggerUrl("讨论服务", "/discussion-service/v3/api-docs", "discussion-service"));
        
        properties.setUrls(urls);
        properties.setConfigUrl("/v3/api-docs/swagger-config");
        return properties;
    }
}
