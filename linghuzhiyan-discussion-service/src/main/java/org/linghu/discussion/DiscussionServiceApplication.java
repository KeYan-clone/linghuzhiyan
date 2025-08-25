package org.linghu.discussion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 讨论服务启动类
 */
@SpringBootApplication
@EnableFeignClients
public class DiscussionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscussionServiceApplication.class, args);
    }
}
