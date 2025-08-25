package org.linghu.experiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.linghu.experiment.domain.Question.QuestionType;

import java.time.LocalDateTime;

/**
 * 题目数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {

    private String id;
    private QuestionType questionType;
    private String content;
    private Object options;       // JSON序列化后的选项内容
    private Object answer;        // JSON序列化后的答案内容
    private String explanation;
    private String tags;
    private String createdAt;
    private String updatedAt;
}
