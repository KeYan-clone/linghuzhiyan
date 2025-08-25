package org.linghu.experiment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.linghu.experiment.domain.Experiment;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperimentDTO {
    private String id;
    private String creator_Id;
    private String name;
    private String description;
    private Experiment.ExperimentStatus status; // "DRAFT", "PUBLISHED"
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
