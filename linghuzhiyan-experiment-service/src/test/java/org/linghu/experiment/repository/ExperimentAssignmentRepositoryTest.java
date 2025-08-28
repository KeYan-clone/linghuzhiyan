package org.linghu.experiment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.ExperimentAssignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExperimentAssignmentRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ExperimentAssignmentRepositoryTest {


    @Autowired
    private ExperimentAssignmentRepository assignmentRepository;

    @Test
    void findByTaskId_WithExistingTaskId_ShouldReturnAssignments() {
        // When
        List<ExperimentAssignment> assignments = assignmentRepository.findByTaskId("task1");

        // Then
        assertNotNull(assignments);
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().allMatch(a -> "task1".equals(a.getTaskId())));
    }

    @Test
    void findByTaskId_WithNonExistingTaskId_ShouldReturnEmptyList() {
        // When
        List<ExperimentAssignment> assignments = assignmentRepository.findByTaskId("nonexistent");

        // Then
        assertNotNull(assignments);
        assertTrue(assignments.isEmpty());
    }

    @Test
    void findByUserId_WithExistingUserId_ShouldReturnAssignments() {
        // When
        List<ExperimentAssignment> assignments = assignmentRepository.findByUserId("user1");

        // Then
        assertNotNull(assignments);
        assertEquals(2, assignments.size());
        assertTrue(assignments.stream().allMatch(a -> "user1".equals(a.getUserId())));
    }

    @Test
    void findByUserId_WithNonExistingUserId_ShouldReturnEmptyList() {
        // When
        List<ExperimentAssignment> assignments = assignmentRepository.findByUserId("nonexistent");

        // Then
        assertNotNull(assignments);
        assertTrue(assignments.isEmpty());
    }

    @Test
    void findByTaskIdAndUserId_WithExistingTaskAndUser_ShouldReturnAssignment() {
        // When
        Optional<ExperimentAssignment> assignment = assignmentRepository.findByTaskIdAndUserId("task1", "user1");

        // Then
        assertTrue(assignment.isPresent());
        assertEquals("task1", assignment.get().getTaskId());
        assertEquals("user1", assignment.get().getUserId());
    }

    @Test
    void findByTaskIdAndUserId_WithNonExistingTaskAndUser_ShouldReturnEmpty() {
        // When
        Optional<ExperimentAssignment> assignment = assignmentRepository.findByTaskIdAndUserId("nonexistent", "user1");

        // Then
        assertFalse(assignment.isPresent());
    }

    @Test
    void deleteByTaskIdAndUserId_WithExistingTaskAndUser_ShouldDeleteAndReturnCount() {
        // When
        long deletedCount = assignmentRepository.deleteByTaskIdAndUserId("task1", "user1");

        // Then
        assertEquals(1, deletedCount);
        Optional<ExperimentAssignment> assignment = assignmentRepository.findByTaskIdAndUserId("task1", "user1");
        assertFalse(assignment.isPresent());
    }

    @Test
    void deleteByTaskIdAndUserId_WithNonExistingTaskAndUser_ShouldReturnZero() {
        // When
        long deletedCount = assignmentRepository.deleteByTaskIdAndUserId("nonexistent", "user1");

        // Then
        assertEquals(0, deletedCount);
    }

    @Test
    void deleteByTaskId_WithExistingTaskId_ShouldDeleteAllAssignmentsAndReturnCount() {
        // When
        long deletedCount = assignmentRepository.deleteByTaskId("task1");

        // Then
        assertEquals(2, deletedCount);
        List<ExperimentAssignment> remaining = assignmentRepository.findByTaskId("task1");
        assertTrue(remaining.isEmpty());
    }

    @Test
    void deleteByTaskId_WithNonExistingTaskId_ShouldReturnZero() {
        // When
        long deletedCount = assignmentRepository.deleteByTaskId("nonexistent");

        // Then
        assertEquals(0, deletedCount);
    }

    @Test
    void countByTaskId_WithExistingTaskId_ShouldReturnCorrectCount() {
        // When
        long count = assignmentRepository.countByTaskId("task1");

        // Then
        assertEquals(2, count);
    }

    @Test
    void countByTaskId_WithNonExistingTaskId_ShouldReturnZero() {
        // When
        long count = assignmentRepository.countByTaskId("nonexistent");

        // Then
        assertEquals(0, count);
    }

    @Test
    void existsByTaskIdAndUserId_WithExistingTaskAndUser_ShouldReturnTrue() {
        // When
        boolean exists = assignmentRepository.existsByTaskIdAndUserId("task1", "user1");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsByTaskIdAndUserId_WithNonExistingTaskAndUser_ShouldReturnFalse() {
        // When
        boolean exists = assignmentRepository.existsByTaskIdAndUserId("nonexistent", "user1");

        // Then
        assertFalse(exists);
    }

    @Test
    void findUserIdsByTaskId_WithExistingTaskId_ShouldReturnUserIds() {
        // When
        List<String> userIds = assignmentRepository.findUserIdsByTaskId("task1");

        // Then
        assertNotNull(userIds);
        assertEquals(2, userIds.size());
        assertTrue(userIds.contains("user1"));
        assertTrue(userIds.contains("user2"));
    }

    @Test
    void findUserIdsByTaskId_WithNonExistingTaskId_ShouldReturnEmptyList() {
        // When
        List<String> userIds = assignmentRepository.findUserIdsByTaskId("nonexistent");

        // Then
        assertNotNull(userIds);
        assertTrue(userIds.isEmpty());
    }

    @Test
    void findTaskIdsByUserId_WithExistingUserId_ShouldReturnTaskIds() {
        // When
        List<String> taskIds = assignmentRepository.findTaskIdsByUserId("user1");

        // Then
        assertNotNull(taskIds);
        assertEquals(2, taskIds.size());
        assertTrue(taskIds.contains("task1"));
        assertTrue(taskIds.contains("task2"));
    }

    @Test
    void findTaskIdsByUserId_WithNonExistingUserId_ShouldReturnEmptyList() {
        // When
        List<String> taskIds = assignmentRepository.findTaskIdsByUserId("nonexistent");

        // Then
        assertNotNull(taskIds);
        assertTrue(taskIds.isEmpty());
    }

    @Test
    void save_WithValidAssignment_ShouldSaveSuccessfully() {
        // Given
        ExperimentAssignment newAssignment = ExperimentAssignment.builder()
                .id(UUID.randomUUID().toString())
                .taskId("task3")
                .userId("user3")
                .assignedAt(new Date())
                .build();

        // When
        ExperimentAssignment saved = assignmentRepository.save(newAssignment);

        // Then
        assertNotNull(saved);
        assertEquals("task3", saved.getTaskId());
        assertEquals("user3", saved.getUserId());
        assertNotNull(saved.getAssignedAt());
    }

}
