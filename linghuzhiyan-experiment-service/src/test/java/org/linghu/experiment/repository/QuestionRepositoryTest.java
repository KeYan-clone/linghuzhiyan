package org.linghu.experiment.repository;

import org.junit.jupiter.api.Test;
import org.linghu.experiment.domain.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QuestionRepository 测试类
 */
@DataJpaTest
@ActiveProfiles("test")
@Sql(scripts = {"/schema.sql", "/data.sql"})
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    void findByQuestionType_WithExistingType_ShouldReturnQuestions() {
        // When
        List<Question> questions = questionRepository.findByQuestionType(Question.QuestionType.SINGLE_CHOICE);

        // Then
        assertNotNull(questions);
        assertEquals(2, questions.size());
        assertEquals(Question.QuestionType.SINGLE_CHOICE, questions.get(0).getQuestionType());
    }

    @Test
    void findByQuestionType_WithNonExistingType_ShouldReturnEmptyList() {
        // 先删除所有题目，然后查询一个不存在的类型
        questionRepository.deleteAll();
        
        // When
        List<Question> questions = questionRepository.findByQuestionType(Question.QuestionType.SINGLE_CHOICE);

        // Then
        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }

    @Test
    void findByQuestionTypeWithPageable_ShouldReturnPagedResults() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);

        // When
        Page<Question> questions = questionRepository.findByQuestionType(Question.QuestionType.SINGLE_CHOICE, pageable);

        // Then
        assertNotNull(questions);
        assertEquals(2, questions.getContent().size());
        assertEquals(2, questions.getTotalElements());
    }

    @Test
    void findByTagsContaining_WithExistingTag_ShouldReturnQuestions() {
        // When
        List<Question> questions = questionRepository.findByTagsContaining("programming");

        // Then
        assertNotNull(questions);
        assertEquals(2, questions.size());
        assertTrue(questions.stream().allMatch(q -> q.getTags().contains("programming")));
    }

    @Test
    void findByTagsContaining_WithNonExistingTag_ShouldReturnEmptyList() {
        // When
        List<Question> questions = questionRepository.findByTagsContaining("nonexistent");

        // Then
        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }

    @Test
    void findByIdIn_WithExistingIds_ShouldReturnQuestions() {
        // Given
        List<String> ids = Arrays.asList("question1", "q2");

        // When
        List<Question> questions = questionRepository.findByIdIn(ids);

        // Then
        assertNotNull(questions);
        assertEquals(2, questions.size());
        assertTrue(questions.stream().map(Question::getId).allMatch(ids::contains));
    }

    @Test
    void findByIdIn_WithNonExistingIds_ShouldReturnEmptyList() {
        // Given
        List<String> ids = Arrays.asList("nonexistent1", "nonexistent2");

        // When
        List<Question> questions = questionRepository.findByIdIn(ids);

        // Then
        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }

    @Test
    void findByIdIn_WithEmptyList_ShouldReturnEmptyList() {
        // Given
        List<String> ids = new ArrayList<>();

        // When
        List<Question> questions = questionRepository.findByIdIn(ids);

        // Then
        assertNotNull(questions);
        assertTrue(questions.isEmpty());
    }

    @Test
    void countByQuestionType_WithExistingType_ShouldReturnCorrectCount() {
        // When
        long count = questionRepository.countByQuestionType(Question.QuestionType.SINGLE_CHOICE);

        // Then
        assertEquals(2, count);
    }

    @Test
    void countByQuestionType_WithNonExistingType_ShouldReturnZero() {
        // 先删除所有题目
        questionRepository.deleteAll();
        
        // When
        long count = questionRepository.countByQuestionType(Question.QuestionType.SINGLE_CHOICE);

        // Then
        assertEquals(0, count);
    }


    @Test
    void save_WithValidQuestion_ShouldSaveSuccessfully() {
        // Given
        Date now = new Date();
        Question newQuestion = Question.builder()
                .id(UUID.randomUUID().toString())
                .questionType(Question.QuestionType.SINGLE_CHOICE)
                .content("What is 2 + 2?")
                .score(new BigDecimal("2.0"))
                .options("{\"A\": \"3\", \"B\": \"4\", \"C\": \"5\", \"D\": \"6\"}")
                .answer("{\"correct\": \"B\"}")
                .explanation("2 + 2 equals 4.")
                .tags("math,arithmetic")
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        Question saved = questionRepository.save(newQuestion);

        // Then
        assertNotNull(saved);
        assertEquals("What is 2 + 2?", saved.getContent());
        assertEquals(Question.QuestionType.SINGLE_CHOICE, saved.getQuestionType());
        assertEquals(new BigDecimal("2.0"), saved.getScore());
    }


    @Test
    void findById_WithExistingId_ShouldReturnQuestion() {
        // When
        Optional<Question> found = questionRepository.findById("question1");

        // Then
        assertTrue(found.isPresent());
        assertEquals("question1", found.get().getId());
        assertEquals("What is the capital of France?", found.get().getContent());
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmpty() {
        // When
        Optional<Question> found = questionRepository.findById("nonexistent");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    void deleteById_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        String idToDelete = "question1";

        // When
        questionRepository.deleteById(idToDelete);

        // Then
        Optional<Question> deleted = questionRepository.findById(idToDelete);
        assertFalse(deleted.isPresent());
    }

    @Test
    void count_ShouldReturnCorrectCount() {
        // When
        long count = questionRepository.count();

        // Then
        assertEquals(5, count); // data.sql中有5个问题
    }

    @Test
    void save_UpdateExistingQuestion_ShouldUpdateSuccessfully() {
        // Given - 先获取现有的问题
        Optional<Question> existing = questionRepository.findById("question1");
        assertTrue(existing.isPresent());
        
        Question question = existing.get();
        question.setContent("Updated question content");
        question.setScore(new BigDecimal("7.5"));
        question.setTags("updated,tags");

        // When
        Question updated = questionRepository.save(question);

        // Then
        assertNotNull(updated);
        assertEquals("Updated question content", updated.getContent());
        assertEquals(new BigDecimal("7.5"), updated.getScore());
        assertEquals("updated,tags", updated.getTags());
    }

    @Test
    void findAll_ShouldReturnAllQuestions() {
        // When
        List<Question> questions = questionRepository.findAll();

        // Then
        assertNotNull(questions);
        assertEquals(5, questions.size()); // data.sql中有5个问题
    }

    @Test
    void deleteAll_ShouldDeleteAllQuestions() {
        // When
        questionRepository.deleteAll();

        // Then
        long count = questionRepository.count();
        assertEquals(0, count);
    }

    @Test
    void existsById_WithExistingId_ShouldReturnTrue() {
        // When
        boolean exists = questionRepository.existsById("question1");

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_WithNonExistingId_ShouldReturnFalse() {
        // When
        boolean exists = questionRepository.existsById("nonexistent");

        // Then
        assertFalse(exists);
    }
}
