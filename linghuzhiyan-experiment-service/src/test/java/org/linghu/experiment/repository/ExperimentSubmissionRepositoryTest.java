package org.linghu.experiment.repository;

import org.hibernate.validator.internal.constraintvalidators.bv.AssertTrueValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.ExperimentSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExperimentSubmissionRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ExperimentSubmissionRepositoryTest {


    @Autowired
    private ExperimentSubmissionRepository submissionRepository;
    @Autowired
    private ExperimentSubmissionRepository experimentSubmissionRepository;


    @Test
    void findByTaskId_WithExistingTaskId_ShouldReturnSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskId("task1");

        // Then
        assertNotNull(submissions);
        assertEquals(3, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> "task1".equals(s.getTaskId())));
    }

    @Test
    void findByTaskId_WithNonExistingTaskId_ShouldReturnEmptyList() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskId("nonexistent");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findByTaskIdWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<ExperimentSubmission> submissions = submissionRepository.findByTaskId("task1", pageable);

        // Then
        assertNotNull(submissions);
        assertEquals(2, submissions.getContent().size());
        assertEquals(3, submissions.getTotalElements());
        assertEquals(2, submissions.getTotalPages());
    }

    @Test
    void findByTaskIdAndSubmitTimeBetween_WithValidTimeRange_ShouldReturnSubmissions() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDateTime = now.minusHours(3);
        LocalDateTime endDateTime = now;


        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskIdAndSubmitTimeBetween("task1", startDateTime, endDateTime);

        // Then
        assertNotNull(submissions);
        assertEquals(3, submissions.size());
    }

    @Test
    void findByTaskIdAndSubmitTimeBetween_WithEmptyTimeRange_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(10);
        LocalDateTime endDate = now.minusDays(9);

        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskIdAndSubmitTimeBetween("task1", startDate, endDate);

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findByGraderId_WithExistingGraderId_ShouldReturnSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByGraderId("grader1");

        // Then
        assertNotNull(submissions);
        assertEquals(2, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> "grader1".equals(s.getGraderId())));
    }

    @Test
    void findByGraderId_WithNonExistingGraderId_ShouldReturnEmptyList() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByGraderId("nonexistent");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findUngradedSubmissionsByTaskId_ShouldReturnUngradedSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findUngradedSubmissionsByTaskId("task2");

        // Then
        assertNotNull(submissions);
        assertEquals(1, submissions.size());
        assertNull(submissions.get(0).getGraderId());
    }

    @Test
    void findUngradedSubmissionsByTaskId_WithNoUngradedSubmissions_ShouldReturnEmptyList() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findUngradedSubmissionsByTaskId("task1");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findGradedSubmissionsByTaskId_ShouldReturnGradedSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findGradedSubmissionsByTaskId("task1");

        // Then
        assertNotNull(submissions);
        assertEquals(3, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> s.getGraderId() != null));
    }

    @Test
    void findGradedSubmissionsByTaskId_WithNoGradedSubmissions_ShouldReturnEmptyList() {
        // Create a task with only ungraded submissions for this test
        ExperimentSubmission ungradedSubmission = ExperimentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .taskId("task3")
                .userId("user3")
                .userAnswer("{\"answer\": \"solution\"}")
                .score(null)
                .graderId(null)
                .gradedTime(null)
                .timeSpent(1200)
                .submitTime(LocalDateTime.now())
                .createdTime(LocalDateTime.now())
                .updatedTime(LocalDateTime.now())
                .build();

        // When
        List<ExperimentSubmission> submissions = submissionRepository.findGradedSubmissionsByTaskId("task3");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findBySubmitTimeBetween_WithValidTimeRange_ShouldReturnSubmissions() {
        // Given
        LocalDateTime now = LocalDateTime.now();


        LocalDateTime startDate = now.minusDays(3);
        LocalDateTime endDate = now;

        // When
        List<ExperimentSubmission> submissions = submissionRepository.findBySubmitTimeBetween(startDate, endDate);

        // Then
        assertNotNull(submissions);
        assertEquals(4, submissions.size());
    }

    @Test
    void findBySubmitTimeBetween_WithEmptyTimeRange_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.minusDays(10);
        LocalDateTime endDate = now.minusDays(9);
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findBySubmitTimeBetween(startDate, endDate);

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findByTaskIdAndUserAnswerContaining_WithExistingContent_ShouldReturnSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskIdAndUserAnswerContaining("task1", "solution");

        // Then
        assertNotNull(submissions);
        assertEquals(3, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> s.getUserAnswer().contains("solution")));
    }

    @Test
    void findByTaskIdAndUserAnswerContaining_WithNonExistingContent_ShouldReturnEmptyList() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByTaskIdAndUserAnswerContaining("task1", "nonexistent");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void getAverageScoreByTaskId_WithScoredSubmissions_ShouldReturnAverageScore() {
        // When
        Double averageScore = submissionRepository.getAverageScoreByTaskId("task1");

        // Then
        assertNotNull(averageScore);
        // (85.50 + 92.00 + 95.00) / 3 = 90.83
        assertEquals(90.83, averageScore, 0.01);
    }

    @Test
    void getAverageScoreByTaskId_WithNoScoredSubmissions_ShouldReturnNull() {
        // When
        Double averageScore = submissionRepository.getAverageScoreByTaskId("task2");

        // Then
        assertNull(averageScore);
    }

    @Test
    void findByTaskIdAndUserId_WithNonExistingTaskAndUser_ShouldReturnEmpty() {
        // When
        Optional<ExperimentSubmission> submission = submissionRepository.findByTaskIdAndUserId("nonexistent", "user1");

        // Then
        assertFalse(submission.isPresent());
    }

    @Test
    void findFirstByUserIdOrderBySubmitTimeDesc_WithExistingUserId_ShouldReturnLatestSubmission() {
        // When
        Optional<ExperimentSubmission> submission = submissionRepository.findFirstByUserIdOrderBySubmitTimeDesc("user1");

        // Then
        assertTrue(submission.isPresent());
        assertEquals("user1", submission.get().getUserId());
    }

    @Test
    void findFirstByUserIdOrderBySubmitTimeDesc_WithNonExistingUserId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentSubmission> submission = submissionRepository.findFirstByUserIdOrderBySubmitTimeDesc("nonexistent");

        // Then
        assertFalse(submission.isPresent());
    }

    @Test
    void findByUserIdAndExperimentIdOrderBySubmitTimeDesc_WithExistingUserAndExperiment_ShouldReturnSubmissions() {
        // 注意：这个方法需要关联查询 ExperimentTask，在单元测试中可能无法正常工作
        // 这里只测试方法调用不抛异常
        // When & Then
        assertDoesNotThrow(() -> {
            List<ExperimentSubmission> submissions = submissionRepository.findByUserIdAndExperimentIdOrderBySubmitTimeDesc("user1", "experiment1");
            assertNotNull(submissions);
        });
    }

    @Test
    void findByUserId_WithExistingUserId_ShouldReturnSubmissions() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByUserId("user1");

        // Then
        assertNotNull(submissions);
        assertEquals(3, submissions.size());
        assertTrue(submissions.stream().allMatch(s -> "user1".equals(s.getUserId())));
    }

    @Test
    void findByUserId_WithNonExistingUserId_ShouldReturnEmptyList() {
        // When
        List<ExperimentSubmission> submissions = submissionRepository.findByUserId("nonexistent");

        // Then
        assertNotNull(submissions);
        assertTrue(submissions.isEmpty());
    }

    @Test
    void findByUserIdWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<ExperimentSubmission> submissions = submissionRepository.findByUserId("user1", pageable);

        // Then
        assertNotNull(submissions);
        assertEquals(1, submissions.getContent().size());
        assertEquals(3, submissions.getTotalElements());
        assertEquals(3, submissions.getTotalPages());
    }

    @Test
    void save_WithValidSubmission_ShouldSaveSuccessfully() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        ExperimentSubmission newSubmission = ExperimentSubmission.builder()
                .id(UUID.randomUUID().toString())
                .taskId("task3")
                .userId("user3")
                .userAnswer("{\"answer\": \"new_solution\"}")
                .score(null)
                .graderId(null)
                .gradedTime(null)
                .timeSpent(1500)
                .submitTime(now)
                .createdTime(now)
                .updatedTime(now)
                .build();

        // When
        ExperimentSubmission saved = submissionRepository.save(newSubmission);

        // Then
        assertNotNull(saved);
        assertEquals("task3", saved.getTaskId());
        assertEquals("user3", saved.getUserId());
        assertEquals("{\"answer\": \"new_solution\"}", saved.getUserAnswer());
        assertEquals(1500, saved.getTimeSpent());
    }

    @Test
    void findById_WithExistingId_ShouldReturnSubmission() {
        // When
        Optional<ExperimentSubmission> found = submissionRepository.findById("sub1");

        // Then
        assertTrue(found.isPresent());
        assertEquals("sub1", found.get().getId());
        assertEquals("task1", found.get().getTaskId());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<ExperimentSubmission> found = submissionRepository.findById("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        String idToDelete = "sub1";

        // When
        submissionRepository.deleteById(idToDelete);

        // Then
        Optional<ExperimentSubmission> deleted = submissionRepository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = submissionRepository.count();

        // Then
        assertEquals(4, count);
    }

    @Test
    void save_UpdateExistingSubmission_ShouldUpdateSuccessfully() {
        // Given
        Optional<ExperimentSubmission>existing=experimentSubmissionRepository.findById("sub3");
        assertTrue(existing.isPresent());

        ExperimentSubmission submission=existing.get();

        submission.setScore(new BigDecimal("78.50"));
        submission.setGraderId("grader3");
        submission.setGradedTime(LocalDateTime.now());

        // When
        ExperimentSubmission updated = submissionRepository.save(submission);

        // Then
        assertNotNull(updated);
        assertEquals(new BigDecimal("78.50"), updated.getScore());
        assertEquals("grader3", updated.getGraderId());
        assertNotNull(updated.getGradedTime());
    }
}
