package org.linghu.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 服务健康检查响应
 * 
 * @author linghu
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HealthCheckResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 服务名称
     */
    private String serviceName;
    
    /**
     * 服务状态
     */
    private String status;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 检查时间
     */
    private LocalDateTime checkTime;
    
    /**
     * 运行时长（毫秒）
     */
    private Long uptime;
    
    /**
     * 额外信息
     */
    private String message;
    
    /**
     * 创建健康响应
     * 
     * @param serviceName 服务名称
     * @param status 状态
     * @return HealthCheckResponse
     */
    public static HealthCheckResponse healthy(String serviceName, String status) {
        return new HealthCheckResponse(
            serviceName, 
            status, 
            "1.0.0", 
            LocalDateTime.now(), 
            System.currentTimeMillis(),
            "Service is running normally"
        );
    }
    
    /**
     * 创建不健康响应
     * 
     * @param serviceName 服务名称
     * @param message 错误信息
     * @return HealthCheckResponse
     */
    public static HealthCheckResponse unhealthy(String serviceName, String message) {
        return new HealthCheckResponse(
            serviceName, 
            "DOWN", 
            "1.0.0", 
            LocalDateTime.now(), 
            System.currentTimeMillis(),
            message
        );
    }
}
