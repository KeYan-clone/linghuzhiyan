package org.linghu.monitor;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 监控服务启动类
 * 
 * @author linghu
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAdminServer
@EnableDiscoveryClient
public class MonitorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonitorServiceApplication.class, args);
        System.out.println("======================================");
        System.out.println("灵狐智验监控服务启动成功！");
        System.out.println("Admin Dashboard: http://localhost:8090");
        System.out.println("Login: admin/admin123");
        System.out.println("======================================");
    }
}
