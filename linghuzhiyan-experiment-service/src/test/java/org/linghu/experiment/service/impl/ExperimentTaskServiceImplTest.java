package org.linghu.experiment.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.experiment.client.UserServiceClient;
import org.linghu.experiment.constants.TaskType;
import org.linghu.experiment.domain.ExperimentTask;
import org.linghu.experiment.dto.ExperimentTaskDTO;
import org.linghu.experiment.dto.ExperimentTaskRequestDTO;
import org.linghu.experiment.repository.ExperimentRepository;
import org.linghu.experiment.repository.ExperimentTaskRepository;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ExperimentTaskServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ExperimentTaskServiceImplTest extends ExperimentTaskServiceImpl{

    @Mock
    private ExperimentTaskRepository experimentTaskRepository;

    @Mock
    private ExperimentRepository experimentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private ExperimentTaskServiceImpl experimentTaskService;

    private ExperimentTask testTask;
    private ExperimentTaskRequestDTO testTaskRequest;

    public ExperimentTaskServiceImplTest(@Mock ExperimentTaskRepository experimentTaskRepository, @Mock ExperimentRepository experimentRepository, @Mock ObjectMapper objectMapper,@Mock UserServiceClient userServiceClient) {
        super(experimentTaskRepository, experimentRepository,objectMapper,userServiceClient);
    }

    @Override
    protected void ensureOwnerOfExperiment(String experimentId, String errorMessage) {
        // no test
    }

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testTask = ExperimentTask.builder()
                .id("task1")
                .experimentId("experiment1")
                .title("Test Task")
                .description("Test Description")
                .taskType(TaskType.CODE)
                .orderNum(1)
                .questionIds("[\"q1\", \"q2\"]")
                .required(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        testTaskRequest = ExperimentTaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .taskType(TaskType.CODE)
                .required(true)
                .question(List.of("q1", "q2"))
                .build();
        experimentTaskService = new ExperimentTaskServiceImplTest(
                experimentTaskRepository,
                experimentRepository,
                objectMapper,
                userServiceClient);
    }

    @Test
    void createTask_WithValidData_ShouldCreateSuccessfully() throws JsonProcessingException {
        // Given
        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findMaxOrderNumByExperimentId("experiment1")).thenReturn(0);
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"q1\", \"q2\"]");
        when(experimentTaskRepository.save(any(ExperimentTask.class))).thenReturn(testTask);

        // When
        ExperimentTaskDTO result = experimentTaskService.createTask("experiment1", testTaskRequest);

        // Then
        assertNotNull(result);
        assertEquals("task1", result.getId());
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskType.CODE, result.getTaskType());
        assertTrue(result.getRequired());
        verify(experimentTaskRepository).save(any(ExperimentTask.class));
    }

    @Test
    void createTask_WithNonExistingExperiment_ShouldThrowException() {
        // Given
        when(experimentRepository.existsById("nonexistent")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.createTask("nonexistent", testTaskRequest);
        });
        assertEquals("实验不存在", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void createTask_WithJsonProcessingException_ShouldThrowException() throws JsonProcessingException {
        // Given
        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findMaxOrderNumByExperimentId("experiment1")).thenReturn(0);
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.createTask("experiment1", testTaskRequest);
        });
        assertEquals("转换问题ID为JSON失败", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void createTask_WithNullQuestion_ShouldCreateWithNullQuestionIds() {
        // Given
        ExperimentTaskRequestDTO requestWithoutQuestion = ExperimentTaskRequestDTO.builder()
                .title("Test Task")
                .description("Test Description")
                .taskType(TaskType.CODE)
                .required(true)
                .question(null)
                .build();

        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findMaxOrderNumByExperimentId("experiment1")).thenReturn(0);
        when(experimentTaskRepository.save(any(ExperimentTask.class))).thenReturn(testTask);

        // When
        ExperimentTaskDTO result = experimentTaskService.createTask("experiment1", requestWithoutQuestion);

        // Then
        assertNotNull(result);
        verify(experimentTaskRepository).save(any(ExperimentTask.class));
    }

    @Test
    void getTasksByExperimentId_WithExistingExperiment_ShouldReturnTasks() {
        // Given
        when(experimentTaskRepository.findByExperimentIdOrderByOrderNumAsc("experiment1"))
                .thenReturn(List.of(testTask));

        // When
        List<ExperimentTaskDTO> result = experimentTaskService.getTasksByExperimentId("experiment1");

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("task1", result.get(0).getId());
        assertEquals("Test Task", result.get(0).getTitle());
    }

    @Test
    void getTasksByExperimentId_WithNonExistingExperiment_ShouldReturnEmptyList() {
        // Given
        when(experimentTaskRepository.findByExperimentIdOrderByOrderNumAsc("nonexistent"))
                .thenReturn(List.of());

        // When
        List<ExperimentTaskDTO> result = experimentTaskService.getTasksByExperimentId("nonexistent");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void updateTask_WithValidData_ShouldUpdateSuccessfully() throws JsonProcessingException {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(objectMapper.writeValueAsString(any())).thenReturn("[\"q1\", \"q2\", \"q3\"]");
        
        ExperimentTask updatedTask = ExperimentTask.builder()
                .id("task1")
                .experimentId("experiment1")
                .title("Updated Task")
                .description("Updated Description")
                .taskType(TaskType.OTHER)
                .orderNum(1)
                .questionIds("[\"q1\", \"q2\", \"q3\"]")
                .required(false)
                .createdAt(testTask.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
                
        when(experimentTaskRepository.save(any(ExperimentTask.class))).thenReturn(updatedTask);

        ExperimentTaskRequestDTO updateRequest = ExperimentTaskRequestDTO.builder()
                .title("Updated Task")
                .description("Updated Description")
                .taskType(TaskType.OTHER)
                .required(false)
                .question(List.of("q1", "q2", "q3"))
                .build();

        // When
        ExperimentTaskDTO result = experimentTaskService.updateTask("task1", updateRequest);

        // Then
        assertNotNull(result);
        assertEquals("task1", result.getId());
        assertEquals("Updated Task", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(TaskType.OTHER, result.getTaskType());
        assertFalse(result.getRequired());
        verify(experimentTaskRepository).save(any(ExperimentTask.class));
    }

    @Test
    void updateTask_WithNonExistingTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.updateTask("nonexistent", testTaskRequest);
        });
        assertEquals("任务不存在", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void updateTask_WithJsonProcessingException_ShouldThrowException() throws JsonProcessingException {
        // Given
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("JSON error") {});

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.updateTask("task1", testTaskRequest);
        });
        assertEquals("转换问题ID为JSON失败", exception.getMessage());
    }

    @Test
    void deleteTask_WithExistingTask_ShouldDeleteSuccessfully() {
        // Given
        when(experimentTaskRepository.existsById("task1")).thenReturn(true);
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));

        // When
        experimentTaskService.deleteTask("task1");

        // Then
        verify(experimentTaskRepository).deleteById("task1");
    }

    @Test
    void deleteTask_WithNonExistingTask_ShouldThrowException() {
        // Given
        when(experimentTaskRepository.existsById("nonexistent")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.deleteTask("nonexistent");
        });
        assertEquals("任务不存在", exception.getMessage());
        verify(experimentTaskRepository, never()).deleteById(any());
    }

    @Test
    void adjustTaskOrder_WithValidData_ShouldAdjustOrderSuccessfully() {
        // Given
        ExperimentTask task2 = ExperimentTask.builder()
                .id("task2")
                .experimentId("experiment1")
                .title("Task 2")
                .description("Description 2")
                .taskType(TaskType.OTHER)
                .orderNum(2)
                .required(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Map<String, String>> taskOrderList = List.of(
                Map.of("id", "task1", "order", "2"),
                Map.of("id", "task2", "order", "1")
        );

        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(testTask));
        when(experimentTaskRepository.findById("task2")).thenReturn(Optional.of(task2));
        when(experimentTaskRepository.save(any(ExperimentTask.class))).thenReturn(testTask);
        when(experimentTaskRepository.findByExperimentIdOrderByOrderNumAsc("experiment1"))
                .thenReturn(List.of(task2, testTask)); // 重新排序后的结果

        // When
        List<ExperimentTaskDTO> result = experimentTaskService.adjustTaskOrder("experiment1", taskOrderList);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(experimentTaskRepository, times(2)).save(any(ExperimentTask.class));
    }

    @Test
    void adjustTaskOrder_WithNonExistingExperiment_ShouldThrowException() {
        // Given
        List<Map<String, String>> taskOrderList = List.of(
                Map.of("id", "task1", "order", "1")
        );
        when(experimentRepository.existsById("nonexistent")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.adjustTaskOrder("nonexistent", taskOrderList);
        });
        assertEquals("实验不存在", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void adjustTaskOrder_WithNonExistingTask_ShouldThrowException() {
        // Given
        List<Map<String, String>> taskOrderList = List.of(
                Map.of("id", "nonexistent", "order", "1")
        );
        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.adjustTaskOrder("experiment1", taskOrderList);
        });
        assertEquals("任务不存在: nonexistent", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void adjustTaskOrder_WithTaskNotBelongingToExperiment_ShouldThrowException() {
        // Given
        ExperimentTask taskFromDifferentExperiment = ExperimentTask.builder()
                .id("task1")
                .experimentId("experiment2") // 不同的实验ID
                .title("Test Task")
                .description("Test Description")
                .taskType(TaskType.CODE)
                .orderNum(1)
                .required(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        List<Map<String, String>> taskOrderList = List.of(
                Map.of("id", "task1", "order", "1")
        );

        when(experimentRepository.existsById("experiment1")).thenReturn(true);
        when(experimentTaskRepository.findById("task1")).thenReturn(Optional.of(taskFromDifferentExperiment));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            experimentTaskService.adjustTaskOrder("experiment1", taskOrderList);
        });
        assertEquals("任务不属于指定实验", exception.getMessage());
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }

    @Test
    void adjustTaskOrder_WithInvalidOrderNumber_ShouldThrowNumberFormatException() {
        // Given
        List<Map<String, String>> taskOrderList = List.of(
                Map.of("id", "task1", "order", "invalid")
        );
        when(experimentRepository.existsById("experiment1")).thenReturn(true);

        // When & Then
        assertThrows(NumberFormatException.class, () -> {
            experimentTaskService.adjustTaskOrder("experiment1", taskOrderList);
        });
        verify(experimentTaskRepository, never()).save(any(ExperimentTask.class));
    }
}
