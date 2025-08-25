package org.linghu.experiment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实验任务提交领域模型，对应数据库中的experiment_submission表
 */
@Entity
@Table(name = "experiment_submission")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ExperimentSubmission {

    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "task_id", nullable = false, length = 36)
    private String taskId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "user_answer", columnDefinition = "JSON")
    private String userAnswer;
    
    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "grader_id", length = 36)
    private String graderId;

    @Column(name = "graded_time")
    private LocalDateTime gradedTime;

    @Column(name = "time_spent")
    private Integer timeSpent;    
    
    // 实验任务关联
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ToString.Exclude
    private ExperimentTask experimentTask;

    // 审计字段
    @CreatedDate
    @Column(name = "created_time", nullable = false, updatable = false)
    private LocalDateTime createdTime;
    
    @LastModifiedDate
    @Column(name = "updated_time")
    private LocalDateTime updatedTime;

    @Column(name = "submit_time", nullable = false)
    @Builder.Default
    private LocalDateTime submitTime = LocalDateTime.now();

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private List<ExperimentEvaluation> evaluations;
}
