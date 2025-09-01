package org.linghu.message.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置
 * 
 * @author linghu
 * @version 1.0.0
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI messageServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("灵狐智验 - 消息服务 API")
                        .description("消息推送、通知管理、邮件发送相关接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("灵狐智验团队")
                                .email("support@linghuzhiyan.com")
                                .url("https://www.linghuzhiyan.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080/api/messages")
                                .description("网关访问地址"),
                        new Server()
                                .url("http://localhost:8005")
                                .description("直接访问地址")
                ));
    }
}
