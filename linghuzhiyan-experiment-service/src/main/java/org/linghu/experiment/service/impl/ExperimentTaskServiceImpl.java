package org.linghu.experiment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.experiment.constants.TaskType;
import org.linghu.experiment.domain.ExperimentTask;
import org.linghu.experiment.dto.ExperimentTaskDTO;
import org.linghu.experiment.dto.ExperimentTaskRequestDTO;
import org.linghu.experiment.dto.SourceCodeFileDTO;
import org.linghu.experiment.repository.ExperimentRepository;
import org.linghu.experiment.repository.ExperimentTaskRepository;
import org.linghu.experiment.service.ExperimentTaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 实验任务管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentTaskServiceImpl implements ExperimentTaskService {
    private final ExperimentTaskRepository experimentTaskRepository;
    private final ExperimentRepository experimentRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public ExperimentTaskDTO createTask(String experimentId, ExperimentTaskRequestDTO requestDTO) {
        // 验证实验是否存在
        if (!experimentRepository.existsById(experimentId)) {
            throw new RuntimeException("实验不存在");
        }

        // 获取当前实验的最大顺序号
        Integer maxOrder = experimentTaskRepository.findMaxOrderNumByExperimentId(experimentId);
        int nextOrder = (maxOrder != null) ? maxOrder + 1 : 1;

        // 处理问题ID - 将Object类型的question字段转换为JSON字符串
        String questionIdsJson = null;
        if (requestDTO.getQuestion() != null) {
            if (requestDTO.getQuestion() instanceof List ||
                    requestDTO.getQuestion().getClass().isArray()) {
                try {
                    questionIdsJson = objectMapper.writeValueAsString(requestDTO.getQuestion());
                } catch (JsonProcessingException e) {
                    log.error("转换问题ID为JSON失败", e);
                    throw new RuntimeException("转换问题ID为JSON失败");
                }
            }
        }

        ExperimentTask task = ExperimentTask.builder()
                .id(UUID.randomUUID().toString())
                .experimentId(experimentId)
                .title(requestDTO.getTitle())
                .description(requestDTO.getDescription())
                .taskType(requestDTO.getTaskType() != null ? requestDTO.getTaskType() : TaskType.OTHER)
                .orderNum(nextOrder)
                .questionIds(questionIdsJson) // 使用处理后的JSON字符串
                .required(requestDTO.getRequired())
                .build();
        ExperimentTask savedTask = experimentTaskRepository.save(task);

        return convertToDTO(savedTask, null);
    }

    @Override
    public List<ExperimentTaskDTO> getTasksByExperimentId(String experimentId) {
        List<ExperimentTask> tasks = experimentTaskRepository.findByExperimentIdOrderByOrderNumAsc(experimentId);

        return tasks.stream()
                .map(task -> convertToDTO(task, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExperimentTaskDTO updateTask(String id, ExperimentTaskRequestDTO requestDTO) {
        ExperimentTask task = experimentTaskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setTitle(requestDTO.getTitle());
        task.setDescription(requestDTO.getDescription());
        task.setTaskType(requestDTO.getTaskType() != null ? requestDTO.getTaskType() : task.getTaskType());
        task.setRequired(requestDTO.getRequired());

        String questionIdsJson = null;
        if (requestDTO.getQuestion() != null) {
            if (requestDTO.getQuestion() instanceof List ||
                    requestDTO.getQuestion().getClass().isArray()) {
                try {
                    questionIdsJson = objectMapper.writeValueAsString(requestDTO.getQuestion());
                } catch (JsonProcessingException e) {
                    log.error("转换问题ID为JSON失败", e);
                    throw new RuntimeException("转换问题ID为JSON失败");
                }
            }
        }
        task.setQuestionIds(questionIdsJson);

        ExperimentTask updatedTask = experimentTaskRepository.save(task);
        return convertToDTO(updatedTask, null);
    }

    @Override
    @Transactional
    public void deleteTask(String id) {
        if (!experimentTaskRepository.existsById(id)) {
            throw new RuntimeException("任务不存在");
        }
        experimentTaskRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<ExperimentTaskDTO> adjustTaskOrder(String experimentId, List<Map<String, String>> taskOrderList) {
        // 校验实验是否存在
        if (!experimentRepository.existsById(experimentId)) {
            throw new RuntimeException("实验不存在");
        }

        // 更新每个任务的顺序
        for (Map<String, String> taskOrder : taskOrderList) {
            String taskId = taskOrder.get("id");
            int order = Integer.parseInt(taskOrder.get("order"));

            ExperimentTask task = experimentTaskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("任务不存在: " + taskId));

            // 确保任务属于指定实验
            if (!task.getExperimentId().equals(experimentId)) {
                throw new RuntimeException("任务不属于指定实验");
            }

            task.setOrderNum(order);
            experimentTaskRepository.save(task);
        }

        // 获取更新后的任务列表
        List<ExperimentTask> tasks = experimentTaskRepository.findByExperimentIdOrderByOrderNumAsc(experimentId);
        return tasks.stream()
                .map(task -> convertToDTO(task, null))
                .collect(Collectors.toList());
    }

    /**
     * 将实验任务实体转换为DTO
     * 
     * @param task            实验任务实体
     * @param sourceCodeFiles 源代码文件列表（可为null）
     * @return 实验任务DTO
     */
    private ExperimentTaskDTO convertToDTO(ExperimentTask task, List<SourceCodeFileDTO> sourceCodeFiles) {
        return ExperimentTaskDTO.builder()
                .id(task.getId())
                .experimentId(task.getExperimentId())
                .title(task.getTitle())
                .description(task.getDescription())
                .taskType(task.getTaskType())
                .orderNum(task.getOrderNum())
                .required(task.getRequired())
                .files(sourceCodeFiles)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
