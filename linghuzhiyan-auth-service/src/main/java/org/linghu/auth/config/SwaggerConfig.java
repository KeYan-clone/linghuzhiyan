package org.linghu.auth.config;

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
    public OpenAPI authServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("灵狐智验 - 认证服务 API")
                        .description("用户登录、注册、Token管理相关接口文档")
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
                                .url("http://localhost:8080/api/auth")
                                .description("网关访问地址"),
                        new Server()
                                .url("http://localhost:8002")
                                .description("直接访问地址")
                ));
    }
}
