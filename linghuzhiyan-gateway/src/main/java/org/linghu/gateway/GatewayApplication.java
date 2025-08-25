package org.linghu.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * API网关启动类
 * 
 * @author linghu
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
        System.out.println("======================================");
        System.out.println("灵狐智验API网关启动成功！");
        System.out.println("Gateway: http://localhost:8080");
        System.out.println("======================================");
    }
}
