package org.linghu.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * 公共模块自动配置
 * 
 * @author linghu
 * @version 1.0.0
 */
@Configuration
@ComponentScan(basePackages = {
    "org.linghu.common.test",
    "org.linghu.common.exception",
    "org.linghu.common.config"
})
@ConditionalOnProperty(name = "linghu.common.enabled", matchIfMissing = true)
public class CommonAutoConfiguration {
    
    // 这个配置类会自动扫描common包下的组件
    // 包括测试控制器、异常处理器、跨域配置等
}
