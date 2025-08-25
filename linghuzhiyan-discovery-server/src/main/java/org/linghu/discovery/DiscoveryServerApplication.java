package org.linghu.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * 服务发现中心启动类
 * 
 * @author linghu
 * @version 1.0.0
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
        System.out.println("======================================");
        System.out.println("灵狐智验服务发现中心启动成功！");
        System.out.println("Eureka Server Dashboard: http://localhost:8761");
        System.out.println("======================================");
    }
}
