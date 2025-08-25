package org.linghu.experiment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 实验评测领域模型
 */
@Entity
@Table(name = "experiment_evaluation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ExperimentEvaluation {
    
    @Id
    @Column(length = 36)
    private String id;
    
    @Column(name = "submission_id", nullable = false, length = 36)
    private String submissionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", insertable = false, updatable = false)
    @ToString.Exclude
    private ExperimentSubmission submission;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private ExperimentTask task;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "additional_info", columnDefinition = "TEXT")
    private String additionalInfo;
    
    @Column(name = "status", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private EvaluationStatus status = EvaluationStatus.PENDING;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    // 代码评测专用字段
    @Column(name = "stdout", columnDefinition = "TEXT")
    private String stdout;
    
    @Column(name = "stderr", columnDefinition = "TEXT")
    private String stderr;
    
    @Column(name = "compiled")
    private Boolean compiled;
    
    @Column(name = "compile_message", columnDefinition = "TEXT")
    private String compileMessage;
    
    @Column(name = "execution_time")
    private Long executionTime;
    
    @Column(name = "memory_usage")
    private Long memoryUsage;
    
    @Column(name = "user_answer", columnDefinition = "JSON")
    private String userAnswer;
    
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;
    
    /**
     * 评测状态枚举
     */
    public enum EvaluationStatus {
        PENDING("待评测"),
        RUNNING("评测中"),
        COMPLETED("已完成"),
        FAILED("评测失败"),
        TIMEOUT("超时"),
        ERROR("错误");
        
        private final String description;
        
        EvaluationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}
