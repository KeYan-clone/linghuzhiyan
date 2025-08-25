package org.linghu.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * 配置中心启动类
 * 
 * @author linghu
 * @version 1.0.0
 */
@SpringBootApplication
@EnableConfigServer
@EnableDiscoveryClient
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
        System.out.println("======================================");
        System.out.println("灵狐智验配置中心启动成功！");
        System.out.println("Config Server: http://localhost:8888");
        System.out.println("======================================");
    }
}
