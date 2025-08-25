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
public class ExperimentRequestDTO {
    private String id;
    private String name;
    private String description;
    private Experiment.ExperimentStatus status; 
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
