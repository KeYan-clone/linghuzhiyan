package org.linghu.experiment.repository;

import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.ExperimentTask;
import org.linghu.experiment.constants.TaskType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExperimentTaskRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ExperimentTaskRepositoryTest {

    @Autowired
    private ExperimentTaskRepository taskRepository;

    @Test
    void findByExperimentId_WithExistingExperimentId_ShouldReturnTasks() {
        // When - 使用data.sql中的实验ID
        List<ExperimentTask> tasks = taskRepository.findByExperimentId("experiment1");

        // Then
        assertNotNull(tasks);
        assertEquals(3, tasks.size()); // experiment1有3个任务：task1, task2, task4
        assertTrue(tasks.stream().allMatch(t -> "experiment1".equals(t.getExperimentId())));
    }

    @Test
    void findByExperimentId_WithNonExistingExperimentId_ShouldReturnEmptyList() {
        // When
        List<ExperimentTask> tasks = taskRepository.findByExperimentId("nonexistent");

        // Then
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void findByExperimentIdOrderByOrderNum_ShouldReturnTasksInOrder() {
        // When - 使用data.sql中的实验ID
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdOrderByOrderNum("experiment1");

        // Then
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        assertEquals(1, tasks.get(0).getOrderNum());
        assertEquals(2, tasks.get(1).getOrderNum());
        assertEquals(3, tasks.get(2).getOrderNum());
    }

    @Test
    void findByExperimentIdOrderByOrderNumAsc_ShouldReturnTasksInAscendingOrder() {
        // When - 使用data.sql中的实验ID
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdOrderByOrderNumAsc("experiment1");

        // Then
        assertNotNull(tasks);
        assertEquals(3, tasks.size());
        // 验证是按 orderNum 升序排列
        for (int i = 0; i < tasks.size() - 1; i++) {
            assertTrue(tasks.get(i).getOrderNum() <= tasks.get(i + 1).getOrderNum());
        }
    }

    @Test
    void findByExperimentIdWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When - 使用data.sql中的实验ID
        Page<ExperimentTask> tasks = taskRepository.findByExperimentId("experiment1", pageable);

        // Then
        assertNotNull(tasks);
        assertEquals(2, tasks.getContent().size());
        assertEquals(3, tasks.getTotalElements());
        assertEquals(2, tasks.getTotalPages());
    }

    @Test
    void countByExperimentId_WithExistingExperimentId_ShouldReturnCorrectCount() {
        // When - 使用data.sql中的实验ID
        long count = taskRepository.countByExperimentId("experiment1");

        // Then
        assertEquals(3, count);
    }

    @Test
    void countByExperimentId_WithNonExistingExperimentId_ShouldReturnZero() {
        // When
        long count = taskRepository.countByExperimentId("nonexistent");

        // Then
        assertEquals(0, count);
    }

    @Test
    void countByExperimentIdAndRequiredTrue_ShouldReturnRequiredTasksCount() {
        // When - 使用data.sql中的实验ID
        long count = taskRepository.countByExperimentIdAndRequiredTrue("experiment1");

        // Then
        assertEquals(2, count); // task1和task4都是必做任务
    }

    @Test
    void countByExperimentIdAndRequiredTrue_WithNonExistingExperimentId_ShouldReturnZero() {
        // When
        long count = taskRepository.countByExperimentIdAndRequiredTrue("nonexistent");

        // Then
        assertEquals(0, count);
    }

    @Test
    void findByExperimentIdAndRequiredTrue_ShouldReturnRequiredTasks() {
        // When - 使用data.sql中的实验ID
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdAndRequiredTrue("experiment1");

        // Then
        assertNotNull(tasks);
        assertEquals(2, tasks.size());
        assertTrue(tasks.stream().allMatch(ExperimentTask::getRequired));
    }

    @Test
    void findByExperimentIdAndRequiredTrue_WithNonExistingExperimentId_ShouldReturnEmptyList() {
        // When
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdAndRequiredTrue("nonexistent");

        // Then
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void findByExperimentIdAndRequiredFalse_ShouldReturnOptionalTasks() {
        // When - 使用data.sql中的实验ID
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdAndRequiredFalse("experiment1");

        // Then
        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertFalse(tasks.get(0).getRequired());
        assertEquals("Task 2", tasks.get(0).getTitle());
    }

    @Test
    void findByExperimentIdAndRequiredFalse_WithNonExistingExperimentId_ShouldReturnEmptyList() {
        // When
        List<ExperimentTask> tasks = taskRepository.findByExperimentIdAndRequiredFalse("nonexistent");

        // Then
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    void findByExperimentIdAndId_WithExistingExperimentAndTaskId_ShouldReturnTask() {
        // When
        Optional<ExperimentTask> task = taskRepository.findByExperimentIdAndId("experiment1", "task1");

        // Then
        assertTrue(task.isPresent());
        assertEquals("task1", task.get().getId());
        assertEquals("experiment1", task.get().getExperimentId());
    }

    @Test
    void findByExperimentIdAndId_WithNonExistingExperimentId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentTask> task = taskRepository.findByExperimentIdAndId("nonexistent", "task1");

        // Then
        assertFalse(task.isPresent());
    }

    @Test
    void findByExperimentIdAndId_WithNonExistingTaskId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentTask> task = taskRepository.findByExperimentIdAndId("experiment1", "nonexistent");

        // Then
        assertFalse(task.isPresent());
    }

    @Test
    void findByExperimentIdAndTitle_WithExistingTitle_ShouldReturnTask() {
        // When
        Optional<ExperimentTask> task = taskRepository.findByExperimentIdAndTitle("experiment1", "Task 1");

        // Then
        assertTrue(task.isPresent());
        assertEquals("Task 1", task.get().getTitle());
        assertEquals("experiment1", task.get().getExperimentId());
    }

    @Test
    void findByExperimentIdAndTitle_WithNonExistingTitle_ShouldReturnEmpty() {
        // When
        Optional<ExperimentTask> task = taskRepository.findByExperimentIdAndTitle("experiment1", "Nonexistent Task");

        // Then
        assertFalse(task.isPresent());
    }

    @Test
    void findMaxOrderNumByExperimentId_WithExistingExperimentId_ShouldReturnMaxOrderNum() {
        // When
        int maxOrderNum = taskRepository.findMaxOrderNumByExperimentId("experiment1");

        // Then
        assertEquals(3, maxOrderNum);
    }

    @Test
    void findMaxOrderNumByExperimentId_WithNonExistingExperimentId_ShouldReturnZero() {
        // When
        int maxOrderNum = taskRepository.findMaxOrderNumByExperimentId("nonexistent");

        // Then
        assertEquals(0, maxOrderNum);
    }

    @Test
    void deleteByExperimentId_WithExistingExperimentId_ShouldDeleteAllTasksAndReturnCount() {
        // When
        long deletedCount = taskRepository.deleteByExperimentId("experiment1");

        // Then
        assertEquals(3, deletedCount);
        List<ExperimentTask> remaining = taskRepository.findByExperimentId("experiment1");
        assertTrue(remaining.isEmpty());
    }

    @Test
    void deleteByExperimentId_WithNonExistingExperimentId_ShouldReturnZero() {
        // When
        long deletedCount = taskRepository.deleteByExperimentId("nonexistent");

        // Then
        assertEquals(0, deletedCount);
    }

    @Test
    void save_WithValidTask_ShouldSaveSuccessfully() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ExperimentTask newTask = ExperimentTask.builder()
                .id(UUID.randomUUID().toString())
                .experimentId("experiment3")
                .title("New Task")
                .description("New task description")
                .questionIds("[\"q6\"]")
                .required(true)
                .orderNum(1)
                .taskType(TaskType.CODE)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        ExperimentTask saved = taskRepository.save(newTask);

        // Then
        assertNotNull(saved);
        assertEquals("New Task", saved.getTitle());
        assertEquals("experiment3", saved.getExperimentId());
        assertTrue(saved.getRequired());
        assertEquals(TaskType.CODE, saved.getTaskType());
    }

    @Test
    void findById_WithExistingId_ShouldReturnTask() {
        // When
        Optional<ExperimentTask> found = taskRepository.findById("task1");

        // Then
        assertTrue(found.isPresent());
        assertEquals("task1", found.get().getId());
        assertEquals("Task 1", found.get().getTitle());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentTask> found = taskRepository.findById("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        String idToDelete = "task1";

        // When
        taskRepository.deleteById(idToDelete);

        // Then
        Optional<ExperimentTask> deleted = taskRepository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = taskRepository.count();

        // Then
        assertEquals(4, count);
    }

    @Test
    void save_UpdateExistingTask_ShouldUpdateSuccessfully() {
        // Given - 先获取现有的任务
        Optional<ExperimentTask> existing = taskRepository.findById("task1");
        assertTrue(existing.isPresent());
        
        ExperimentTask task = existing.get();
        task.setTitle("Updated Task Title");
        task.setDescription("Updated description");
        task.setRequired(false);
        task.setOrderNum(10);

        // When
        ExperimentTask updated = taskRepository.save(task);

        // Then
        assertNotNull(updated);
        assertEquals("Updated Task Title", updated.getTitle());
        assertEquals("Updated description", updated.getDescription());
        assertFalse(updated.getRequired());
        assertEquals(10, updated.getOrderNum());
    }
}
