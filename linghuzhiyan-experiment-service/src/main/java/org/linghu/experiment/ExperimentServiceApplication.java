package org.linghu.experiment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 实验服务启动类
 * 
 * @author linghu
 */
@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
public class ExperimentServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ExperimentServiceApplication.class, args);
    }
}
