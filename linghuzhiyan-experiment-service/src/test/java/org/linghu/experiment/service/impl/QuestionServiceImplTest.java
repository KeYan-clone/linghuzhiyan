package org.linghu.experiment.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.experiment.client.UserServiceClient;
import org.linghu.experiment.domain.Question;
import org.linghu.experiment.dto.QuestionDTO;
import org.linghu.experiment.dto.QuestionRequestDTO;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.repository.QuestionRepository;
import org.linghu.experiment.utils.JsonUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * QuestionServiceImpl 单元测试
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private QuestionServiceImpl questionService;

    private UserDTO testUser;
    private Question testQuestion;
    private QuestionRequestDTO testQuestionRequest;

    @BeforeEach
    void setUp() {
        testUser = new UserDTO();
        testUser.setId("user1");
        testUser.setUsername("testuser");

        testQuestion = Question.builder()
                .id("question1")
                .questionType(Question.QuestionType.SINGLE_CHOICE)
                .content("What is the capital of France?")
                .score(new BigDecimal("5.0"))
                .options("{\"A\": \"London\", \"B\": \"Paris\", \"C\": \"Berlin\", \"D\": \"Madrid\"}")
                .answer("{\"correct\": \"B\"}")
                .explanation("Paris is the capital city of France.")
                .tags("geography,capital,france")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        testQuestionRequest = QuestionRequestDTO.builder()
                .questionType(Question.QuestionType.SINGLE_CHOICE)
                .content("What is the capital of France?")
                .score(new BigDecimal("5.0"))
                .options(Map.of("A", "London", "B", "Paris", "C", "Berlin", "D", "Madrid"))
                .answer(Map.of("correct", "B"))
                .explanation("Paris is the capital city of France.")
                .tags("geography,capital,france")
                .build();
    }

    @Test
    void createQuestion_WithValidData_ShouldCreateSuccessfully() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            when(userServiceClient.getUserByUsername("testuser")).thenReturn(testUser);
            when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
            jsonUtilsMock.when(() -> JsonUtils.toJsonString(any())).thenReturn("{\"test\": \"value\"}");
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            QuestionDTO result = questionService.createQuestion(testQuestionRequest, "testuser");

            // Then
            assertNotNull(result);
            assertEquals("question1", result.getId());
            assertEquals(Question.QuestionType.SINGLE_CHOICE, result.getQuestionType());
            assertEquals("What is the capital of France?", result.getContent());
            verify(questionRepository).save(any(Question.class));
        }
    }

    @Test
    void createQuestion_WithNonExistentUser_ShouldThrowException() {
        // Given
        when(userServiceClient.getUserByUsername("nonexistent")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.createQuestion(testQuestionRequest, "nonexistent");
        });
        assertEquals("用户不存在", exception.getMessage());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void getQuestionById_WithExistingId_ShouldReturnQuestion() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            when(questionRepository.findById("question1")).thenReturn(Optional.of(testQuestion));
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            QuestionDTO result = questionService.getQuestionById("question1");

            // Then
            assertNotNull(result);
            assertEquals("question1", result.getId());
            assertEquals("What is the capital of France?", result.getContent());
        }
    }

    @Test
    void getQuestionById_WithNonExistingId_ShouldThrowException() {
        // Given
        when(questionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.getQuestionById("nonexistent");
        });
        assertEquals("题目不存在", exception.getMessage());
    }

    @Test
    void updateQuestion_WithValidData_ShouldUpdateSuccessfully() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            when(questionRepository.findById("question1")).thenReturn(Optional.of(testQuestion));
            when(questionRepository.save(any(Question.class))).thenReturn(testQuestion);
            jsonUtilsMock.when(() -> JsonUtils.toJsonString(any())).thenReturn("{\"test\": \"value\"}");
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            QuestionDTO result = questionService.updateQuestion("question1", testQuestionRequest);

            // Then
            assertNotNull(result);
            assertEquals("question1", result.getId());
            verify(questionRepository).save(any(Question.class));
        }
    }

    @Test
    void updateQuestion_WithNonExistingId_ShouldThrowException() {
        // Given
        when(questionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.updateQuestion("nonexistent", testQuestionRequest);
        });
        assertEquals("题目不存在", exception.getMessage());
        verify(questionRepository, never()).save(any(Question.class));
    }

    @Test
    void deleteQuestion_WithExistingId_ShouldDeleteSuccessfully() {
        // Given
        when(questionRepository.findById("question1")).thenReturn(Optional.of(testQuestion));

        // When
        questionService.deleteQuestion("question1");

        // Then
        verify(questionRepository).delete(testQuestion);
    }

    @Test
    void deleteQuestion_WithNonExistingId_ShouldThrowException() {
        // Given
        when(questionRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.deleteQuestion("nonexistent");
        });
        assertEquals("题目不存在", exception.getMessage());
        verify(questionRepository, never()).delete(any(Question.class));
    }

    @Test
    void getQuestions_WithTypeFilter_ShouldReturnFilteredQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Question> questionPage = new PageImpl<>(List.of(testQuestion));
            when(questionRepository.findByQuestionType(Question.QuestionType.SINGLE_CHOICE, pageable)).thenReturn(questionPage);
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            Page<QuestionDTO> result = questionService.getQuestions(Question.QuestionType.SINGLE_CHOICE, null, null, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("question1", result.getContent().get(0).getId());
        }
    }

    @Test
    void getQuestions_WithNoFilters_ShouldReturnAllQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Question> questionPage = new PageImpl<>(List.of(testQuestion));
            when(questionRepository.findAll(pageable)).thenReturn(questionPage);
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            Page<QuestionDTO> result = questionService.getQuestions(null, null, null, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }
    }

    @Test
    void searchQuestions_WithValidKeyword_ShouldReturnMatchingQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            when(questionRepository.findByContentContaining("France")).thenReturn(List.of(testQuestion));
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            List<QuestionDTO> result = questionService.searchQuestions("France");

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("question1", result.get(0).getId());
        }
    }

    @Test
    void searchQuestions_WithEmptyKeyword_ShouldThrowException() {
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.searchQuestions("");
        });
        assertEquals("搜索关键词不能为空", exception.getMessage());
    }

    @Test
    void getQuestionTypes_ShouldReturnAllTypes() {
        // When
        List<Question.QuestionType> result = questionService.getQuestionTypes();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(Question.QuestionType.SINGLE_CHOICE));
        assertTrue(result.contains(Question.QuestionType.MULTIPLE_CHOICE));
        assertTrue(result.contains(Question.QuestionType.FILL_BLANK));
        assertTrue(result.contains(Question.QuestionType.QA));
    }

    @Test
    void getAllTags_WithQuestionsHavingTags_ShouldReturnAllUniqueTags() {
        // Given
        Question question1 = Question.builder().tags("tag1,tag2,tag3").build();
        Question question2 = Question.builder().tags("tag2,tag4").build();
        when(questionRepository.findAll()).thenReturn(List.of(question1, question2));

        // When
        Set<String> result = questionService.getAllTags();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains("tag1"));
        assertTrue(result.contains("tag2"));
        assertTrue(result.contains("tag3"));
        assertTrue(result.contains("tag4"));
    }

    @Test
    void getAllTags_WithQuestionsHavingNoTags_ShouldReturnEmptySet() {
        // Given
        Question question1 = Question.builder().tags("").build();
        Question question2 = Question.builder().tags(null).build();
        when(questionRepository.findAll()).thenReturn(List.of(question1, question2));

        // When
        Set<String> result = questionService.getAllTags();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getQuestionsByTag_WithValidTag_ShouldReturnMatchingQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            when(questionRepository.findByTagsContaining("geography")).thenReturn(List.of(testQuestion));
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            Page<QuestionDTO> result = questionService.getQuestionsByTag("geography", pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("question1", result.getContent().get(0).getId());
        }
    }

    @Test
    void getQuestionsByTag_WithEmptyTag_ShouldThrowException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.getQuestionsByTag("", pageable);
        });
        assertEquals("标签不能为空", exception.getMessage());
    }

    @Test
    void getQuestionsByType_WithValidType_ShouldReturnMatchingQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Question> questionPage = new PageImpl<>(List.of(testQuestion));
            when(questionRepository.findByQuestionType(Question.QuestionType.SINGLE_CHOICE, pageable)).thenReturn(questionPage);
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            Page<QuestionDTO> result = questionService.getQuestionsByType(Question.QuestionType.SINGLE_CHOICE, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("question1", result.getContent().get(0).getId());
        }
    }

    @Test
    void getQuestionsByType_WithNullType_ShouldThrowException() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            questionService.getQuestionsByType(null, pageable);
        });
        assertEquals("题目类型不能为空", exception.getMessage());
    }

    @Test
    void getQuestionsByIds_WithValidIds_ShouldReturnMatchingQuestions() {
        try (MockedStatic<JsonUtils> jsonUtilsMock = mockStatic(JsonUtils.class)) {
            // Given
            List<String> ids = List.of("question1", "question2");
            when(questionRepository.findByIdIn(ids)).thenReturn(List.of(testQuestion));
            jsonUtilsMock.when(() -> JsonUtils.parseObject(anyString(), eq(Object.class))).thenReturn(Map.of("test", "value"));

            // When
            List<QuestionDTO> result = questionService.getQuestionsByIds(ids);

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("question1", result.get(0).getId());
        }
    }

    @Test
    void getQuestionsByIds_WithEmptyIds_ShouldReturnEmptyList() {
        // When
        List<QuestionDTO> result = questionService.getQuestionsByIds(new ArrayList<>());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(questionRepository, never()).findByIdIn(any());
    }

    @Test
    void getQuestionsByIds_WithNullIds_ShouldReturnEmptyList() {
        // When
        List<QuestionDTO> result = questionService.getQuestionsByIds(null);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(questionRepository, never()).findByIdIn(any());
    }
}
