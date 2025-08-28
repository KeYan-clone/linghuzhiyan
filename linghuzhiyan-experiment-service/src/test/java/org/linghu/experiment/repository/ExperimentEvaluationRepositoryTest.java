package org.linghu.experiment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.ExperimentEvaluation;
import org.linghu.experiment.domain.ExperimentSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExperimentEvaluationRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ExperimentEvaluationRepositoryTest {


    @Autowired
    private ExperimentEvaluationRepository evaluationRepository;


    @Test
    void findBySubmissionId_WithExistingSubmissionId_ShouldReturnEvaluations() {
        // When
        List<ExperimentEvaluation> evaluations = evaluationRepository.findBySubmissionId("sub1");

        // Then
        assertNotNull(evaluations);
        assertEquals(2, evaluations.size());
        assertTrue(evaluations.stream().allMatch(e -> "sub1".equals(e.getSubmissionId())));
    }

    @Test
    void findBySubmissionId_WithNonExistingSubmissionId_ShouldReturnEmptyList() {
        // When
        List<ExperimentEvaluation> evaluations = evaluationRepository.findBySubmissionId("nonexistent");

        // Then
        assertNotNull(evaluations);
        assertTrue(evaluations.isEmpty());
    }

    @Test
    void findFirstBySubmissionIdOrderByIdDesc_WithExistingSubmissionId_ShouldReturnLatestEvaluation() {
        // When
        Optional<ExperimentEvaluation> evaluation = evaluationRepository.findFirstBySubmissionIdOrderByIdDesc("sub1");

        // Then
        assertTrue(evaluation.isPresent());
        assertEquals("sub1", evaluation.get().getSubmissionId());

    }

    @Test
    void findFirstBySubmissionIdOrderByIdDesc_WithNonExistingSubmissionId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentEvaluation> evaluation = evaluationRepository.findFirstBySubmissionIdOrderByIdDesc("nonexistent");

        // Then
        assertFalse(evaluation.isPresent());
    }

    @Test
    void findByUserIdAndTaskIdOrderByIdDesc_WithExistingUserAndTask_ShouldReturnEvaluationsInDescOrder() {
        // When
        List<ExperimentEvaluation> evaluations = evaluationRepository.findByUserIdAndTaskIdOrderByIdDesc("user1", "task1");

        // Then
        assertNotNull(evaluations);
        assertEquals(2, evaluations.size());
        assertTrue(evaluations.stream().allMatch(e -> "user1".equals(e.getUserId()) && "task1".equals(e.getTaskId())));
    }

    @Test
    void findByUserIdAndTaskIdOrderByIdDesc_WithNonExistingUserAndTask_ShouldReturnEmptyList() {
        // When
        List<ExperimentEvaluation> evaluations = evaluationRepository.findByUserIdAndTaskIdOrderByIdDesc("nonexistent", "task1");

        // Then
        assertNotNull(evaluations);
        assertTrue(evaluations.isEmpty());
    }

    @Test
    void save_WithValidEvaluation_ShouldSaveSuccessfully() {
        // Given
        ExperimentEvaluation newEvaluation = ExperimentEvaluation.builder()
                .id(UUID.randomUUID().toString())
                .submissionId("sub3")
                .userId("user3")
                .taskId("task3")
                .status(ExperimentEvaluation.EvaluationStatus.RUNNING)
                .score(null)
                .feedback(null)
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();

        // When
        ExperimentEvaluation saved = evaluationRepository.save(newEvaluation);

        // Then
        assertNotNull(saved);
        assertEquals("sub3", saved.getSubmissionId());
        assertEquals("user3", saved.getUserId());
        assertEquals("task3", saved.getTaskId());
        assertEquals(ExperimentEvaluation.EvaluationStatus.RUNNING, saved.getStatus());
    }


    @Test
    void findById_WithExistingId_ShouldReturnEvaluation() {
        // When
        Optional<ExperimentEvaluation> found = evaluationRepository.findById("eval1");

        // Then
        assertTrue(found.isPresent());
        assertEquals("eval1", found.get().getId());
        assertEquals("sub1", found.get().getSubmissionId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentEvaluation> found = evaluationRepository.findById("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        String idToDelete = "eval1";

        // When
        evaluationRepository.deleteById(idToDelete);

        // Then
        Optional<ExperimentEvaluation> deleted = evaluationRepository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = evaluationRepository.count();

        // Then
        assertEquals(3, count);
    }

    @Test
    void save_UpdateExistingEvaluation_ShouldUpdateSuccessfully() {
        // Given
        Optional<ExperimentEvaluation>existing=evaluationRepository.findById("eval1");
        assertTrue(existing.isPresent());

        ExperimentEvaluation testEvaluation1=existing.get();
        testEvaluation1.setScore(new BigDecimal("95.00"));
        testEvaluation1.setStatus(ExperimentEvaluation.EvaluationStatus.COMPLETED);
        testEvaluation1.setFeedback("Updated evaluation feedback");

        // When
        ExperimentEvaluation updated = evaluationRepository.save(testEvaluation1);

        // Then
        assertNotNull(updated);
        assertEquals(new BigDecimal("95.00"), updated.getScore());
        assertEquals(ExperimentEvaluation.EvaluationStatus.COMPLETED, updated.getStatus());
        assertEquals("Updated evaluation feedback", updated.getFeedback());
    }
}
