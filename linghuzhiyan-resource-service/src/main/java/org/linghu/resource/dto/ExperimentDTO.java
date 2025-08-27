package org.linghu.resource.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 实验信息DTO - 用于与实验服务通信
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentDTO {

    private String id;
    
    private String creator_Id;
    
    private String name;
    
    private String description;
    
    private String status; // "DRAFT", "PUBLISHED"
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Boolean isActive;
}
