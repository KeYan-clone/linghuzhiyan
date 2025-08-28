package org.linghu.experiment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.Experiment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ExperimentRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class ExperimentRepositoryTest {

    @Autowired
    private ExperimentRepository experimentRepository;

    @Test
    void findByCreatorId_WithExistingCreatorId_ShouldReturnExperiments() {
        // When
        List<Experiment> experiments = experimentRepository.findByCreatorId("creator1");

        // Then
        assertNotNull(experiments);
        assertEquals(2, experiments.size());
        assertTrue(experiments.stream().allMatch(e -> "creator1".equals(e.getCreatorId())));
    }

    @Test
    void findByCreatorId_WithNonExistingCreatorId_ShouldReturnEmptyList() {
        // When
        List<Experiment> experiments = experimentRepository.findByCreatorId("nonexistent");

        // Then
        assertNotNull(experiments);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void findByCreatorIdWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Experiment> experiments = experimentRepository.findByCreatorId("creator1", pageable);

        // Then
        assertNotNull(experiments);
        assertEquals(1, experiments.getContent().size());
        assertEquals(2, experiments.getTotalElements());
        assertEquals(2, experiments.getTotalPages());
    }

    @Test
    void findByStatus_WithExistingStatus_ShouldReturnExperiments() {
        // When
        List<Experiment> experiments = experimentRepository.findByStatus(Experiment.ExperimentStatus.PUBLISHED);

        // Then
        assertNotNull(experiments);
        assertEquals(2, experiments.size());
        assertTrue(experiments.stream().allMatch(e -> Experiment.ExperimentStatus.PUBLISHED.equals(e.getStatus())));
    }

    @Test
    void findByStatus_WithNonExistingStatus_ShouldReturnEmptyList() {
        // When
        List<Experiment> experiments = experimentRepository.findByStatus(Experiment.ExperimentStatus.ARCHIVED);

        // Then
        assertNotNull(experiments);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void findByStatusWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 1);

        // When
        Page<Experiment> experiments = experimentRepository.findByStatus(Experiment.ExperimentStatus.PUBLISHED, pageable);

        // Then
        assertNotNull(experiments);
        assertEquals(1, experiments.getContent().size());
        assertEquals(2, experiments.getTotalElements());
        assertEquals(2, experiments.getTotalPages());
    }

    @Test
    void findByCreatorIdAndStatus_WithExistingCreatorAndStatus_ShouldReturnExperiments() {
        // When
        List<Experiment> experiments = experimentRepository.findByCreatorIdAndStatus("creator1", Experiment.ExperimentStatus.PUBLISHED);

        // Then
        assertNotNull(experiments);
        assertEquals(1, experiments.size());
        assertEquals("creator1", experiments.get(0).getCreatorId());
        assertEquals(Experiment.ExperimentStatus.PUBLISHED, experiments.get(0).getStatus());
    }

    @Test
    void findByCreatorIdAndStatus_WithNonExistingCreatorAndStatus_ShouldReturnEmptyList() {
        // When
        List<Experiment> experiments = experimentRepository.findByCreatorIdAndStatus("nonexistent", Experiment.ExperimentStatus.PUBLISHED);

        // Then
        assertNotNull(experiments);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void findByCreatorIdAndStatusWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Experiment> experiments = experimentRepository.findByCreatorIdAndStatus("creator1", Experiment.ExperimentStatus.PUBLISHED, pageable);

        // Then
        assertNotNull(experiments);
        assertEquals(1, experiments.getContent().size());
        assertEquals(1, experiments.getTotalElements());
    }



    @Test
    void findExperimentsInTimeRange_WithValidTimeRange_ShouldReturnExperiments() {
        // Given
        LocalDateTime now = LocalDateTime.now();


        LocalDateTime startDate = now.minusDays(3);
        LocalDateTime endDate = now.plusDays(3);

        // When
        List<Experiment> experiments = experimentRepository.findExperimentsInTimeRange(startDate, endDate);

        // Then
        assertNotNull(experiments);
        // 应该返回所有实验，因为时间范围覆盖了所有实验
        assertEquals(3, experiments.size());
    }

    @Test
    void findExperimentsInTimeRange_WithEmptyTimeRange_ShouldReturnEmptyList() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.plusDays(10);
        LocalDateTime endDate = now.plusDays(11);

        // When
        List<Experiment> experiments = experimentRepository.findExperimentsInTimeRange(startDate, endDate);

        // Then
        assertNotNull(experiments);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void findActiveExperiments_WithCurrentDate_ShouldReturnActiveExperiments() {
        // Given
        LocalDateTime currentDate =LocalDateTime.now();

        // When
        List<Experiment> experiments = experimentRepository.findActiveExperiments(currentDate);

        // Then
        assertNotNull(experiments);
        // 应该只返回当前正在进行中且已发布的实验
        assertEquals(1, experiments.size());
        assertEquals("experiment1", experiments.get(0).getId());
    }

    @Test
    void findActiveExperiments_WithFutureDate_ShouldReturnEmptyList() {
        // Given
        LocalDateTime futureDate = LocalDateTime.now().plusDays(10);

        // When
        List<Experiment> experiments = experimentRepository.findActiveExperiments(futureDate);

        // Then
        assertNotNull(experiments);
        assertTrue(experiments.isEmpty());
    }

    @Test
    void findActiveExperimentsWithPageable_ShouldReturnPagedResults() {
        // Given
        LocalDateTime currentDate = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Experiment> experiments = experimentRepository.findActiveExperiments(currentDate, pageable);

        // Then
        assertNotNull(experiments);
        assertEquals(1, experiments.getContent().size());
        assertEquals(1, experiments.getTotalElements());
    }

    @Test
    void findByIdAndCreatorId_WithExistingIdAndCreator_ShouldReturnExperiment() {
        // When
        Optional<Experiment> experiment = experimentRepository.findByIdAndCreatorId("experiment1", "creator1");

        // Then
        assertTrue(experiment.isPresent());
        assertEquals("experiment1", experiment.get().getId());
        assertEquals("creator1", experiment.get().getCreatorId());
    }

    @Test
    void findByIdAndCreatorId_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Experiment> experiment = experimentRepository.findByIdAndCreatorId("nonexistent", "creator1");

        // Then
        assertFalse(experiment.isPresent());
    }

    @Test
    void findByIdAndCreatorId_WithWrongCreator_ShouldReturnEmpty() {
        // When
        Optional<Experiment> experiment = experimentRepository.findByIdAndCreatorId("experiment1", "wrongcreator");

        // Then
        assertFalse(experiment.isPresent());
    }

    @Test
    void save_WithValidExperiment_ShouldSaveSuccessfully() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        Experiment newExperiment = Experiment.builder()
                .id(UUID.randomUUID().toString())
                .name("New Test Experiment")
                .description("New test experiment description")
                .creatorId("creator3")
                .status(Experiment.ExperimentStatus.DRAFT)
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(2))
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        Experiment saved = experimentRepository.save(newExperiment);

        // Then
        assertNotNull(saved);
        assertEquals("New Test Experiment", saved.getName());
        assertEquals("creator3", saved.getCreatorId());
        assertEquals(Experiment.ExperimentStatus.DRAFT, saved.getStatus());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        String idToDelete = "experiment1";

        // When
        experimentRepository.deleteById(idToDelete);

        // Then
        Optional<Experiment> deleted = experimentRepository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = experimentRepository.count();

        // Then
        assertEquals(3, count);
    }

    @Test
    void save_UpdateExistingExperiment_ShouldUpdateSuccessfully() {
        // Given
        Optional<Experiment>existing=experimentRepository.findById("experiment1");
        assertTrue(existing.isPresent());

        Experiment testExperiment1=existing.get();
        testExperiment1.setName("Updated Experiment Name");
        testExperiment1.setDescription("Updated description");
        testExperiment1.setStatus(Experiment.ExperimentStatus.ARCHIVED);

        // When
        Experiment updated = experimentRepository.save(testExperiment1);

        // Then
        assertNotNull(updated);
        assertEquals("Updated Experiment Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals(Experiment.ExperimentStatus.ARCHIVED, updated.getStatus());
    }
}
