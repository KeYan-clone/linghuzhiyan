package org.linghu.experiment.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.experiment.dto.ExperimentDTO;
import org.linghu.experiment.service.ExperimentService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ExperimentInternalController 单元测试 - 使用纯单元测试方式
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("实验内部API控制器测试")
class ExperimentInternalControllerTest {

    @Mock
    private ExperimentService experimentService;

    @InjectMocks
    private ExperimentInternalController experimentInternalController;

    private ExperimentDTO sampleExperimentDTO;

    @BeforeEach
    void setUp() {
        // 创建测试实验DTO
        sampleExperimentDTO = ExperimentDTO.builder()
                .id("exp123")
                .name("测试实验名称")
                .description("测试实验描述")
                .creator_Id("teacher123")
                .status(org.linghu.experiment.domain.Experiment.ExperimentStatus.PUBLISHED)
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .build();
    }

    @Nested
    @DisplayName("根据ID获取实验信息测试")
    class GetExperimentByIdTests {

        @Test
        @DisplayName("成功根据ID获取实验信息")
        void shouldGetExperimentByIdSuccessfully() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(sampleExperimentDTO);

            // when
            ExperimentDTO response = experimentInternalController.getExperimentById("exp123");

            // then
            assertThat(response).isNotNull();
            assertThat(response).isEqualTo(sampleExperimentDTO);
            assertThat(response.getId()).isEqualTo("exp123");
            assertThat(response.getName()).isEqualTo("测试实验名称");

            verify(experimentService).getExperimentById("exp123");
        }

        @Test
        @DisplayName("获取不存在的实验信息")
        void shouldHandleGetNonexistentExperiment() {
            // given
            when(experimentService.getExperimentById("nonexistent"))
                    .thenThrow(new RuntimeException("实验未找到"));

            // when & then
            try {
                experimentInternalController.getExperimentById("nonexistent");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("实验未找到");
            }

            verify(experimentService).getExperimentById("nonexistent");
        }

        @Test
        @DisplayName("获取实验信息时服务返回null")
        void shouldHandleNullExperiment() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(null);

            // when
            ExperimentDTO response = experimentInternalController.getExperimentById("exp123");

            // then
            assertThat(response).isNull();

            verify(experimentService).getExperimentById("exp123");
        }
    }

    @Nested
    @DisplayName("验证实验是否存在测试")
    class ExperimentExistsTests {

        @Test
        @DisplayName("验证存在的实验")
        void shouldReturnTrueForExistingExperiment() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(sampleExperimentDTO);

            // when
            Boolean exists = experimentInternalController.experimentExists("exp123");

            // then
            assertThat(exists).isTrue();

            verify(experimentService).getExperimentById("exp123");
        }

        @Test
        @DisplayName("验证不存在的实验")
        void shouldReturnFalseForNonexistentExperiment() {
            // given
            when(experimentService.getExperimentById("nonexistent"))
                    .thenThrow(new RuntimeException("实验未找到"));

            // when
            Boolean exists = experimentInternalController.experimentExists("nonexistent");

            // then
            assertThat(exists).isFalse();

            verify(experimentService).getExperimentById("nonexistent");
        }

        @Test
        @DisplayName("验证实验时服务返回null")
        void shouldReturnFalseWhenServiceReturnsNull() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(null);

            // when
            Boolean exists = experimentInternalController.experimentExists("exp123");

            // then
            assertThat(exists).isFalse();

            verify(experimentService).getExperimentById("exp123");
        }

        @Test
        @DisplayName("验证实验时服务抛出其他异常")
        void shouldReturnFalseOnServiceException() {
            // given
            when(experimentService.getExperimentById("exp123"))
                    .thenThrow(new RuntimeException("服务异常"));

            // when
            Boolean exists = experimentInternalController.experimentExists("exp123");

            // then
            assertThat(exists).isFalse();

            verify(experimentService).getExperimentById("exp123");
        }
    }

    @Nested
    @DisplayName("批量验证实验是否存在测试")
    class BatchExperimentExistsTests {

        @Test
        @DisplayName("批量验证存在和不存在的实验")
        void shouldBatchVerifyExperiments() {
            // given
            List<String> experimentIds = Arrays.asList("exp123", "exp456", "nonexistent");
            
            ExperimentDTO anotherExperiment = ExperimentDTO.builder()
                    .id("exp456")
                    .name("另一个实验")
                    .description("另一个实验描述")
                    .creator_Id("teacher456")
                    .status(org.linghu.experiment.domain.Experiment.ExperimentStatus.DRAFT)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusDays(14))
                    .build();

            when(experimentService.getExperimentById("exp123")).thenReturn(sampleExperimentDTO);
            when(experimentService.getExperimentById("exp456")).thenReturn(anotherExperiment);
            when(experimentService.getExperimentById("nonexistent"))
                    .thenThrow(new RuntimeException("实验未找到"));

            // when
            Map<String, Boolean> result = experimentInternalController.batchExperimentExists(experimentIds);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result.get("exp123")).isTrue();
            assertThat(result.get("exp456")).isTrue();
            assertThat(result.get("nonexistent")).isFalse();

            verify(experimentService).getExperimentById("exp123");
            verify(experimentService).getExperimentById("exp456");
            verify(experimentService).getExperimentById("nonexistent");
        }

        @Test
        @DisplayName("批量验证空列表")
        void shouldHandleEmptyExperimentIdsList() {
            // given
            List<String> emptyList = Arrays.asList();

            // when
            Map<String, Boolean> result = experimentInternalController.batchExperimentExists(emptyList);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(experimentService, never()).getExperimentById(any());
        }

        @Test
        @DisplayName("批量验证包含null值的列表")
        void shouldHandleListWithNullValues() {
            // given
            List<String> idsWithNull = Arrays.asList("exp123", null, "exp456");
            
            when(experimentService.getExperimentById("exp123")).thenReturn(sampleExperimentDTO);
            when(experimentService.getExperimentById(null))
                    .thenThrow(new RuntimeException("ID不能为null"));
            when(experimentService.getExperimentById("exp456")).thenReturn(sampleExperimentDTO);

            // when
            Map<String, Boolean> result = experimentInternalController.batchExperimentExists(idsWithNull);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(3);
            assertThat(result.get("exp123")).isTrue();
            assertThat(result.get(null)).isFalse();
            assertThat(result.get("exp456")).isTrue();

            verify(experimentService).getExperimentById("exp123");
            verify(experimentService).getExperimentById(null);
            verify(experimentService).getExperimentById("exp456");
        }

        @Test
        @DisplayName("批量验证所有实验都不存在")
        void shouldHandleAllNonexistentExperiments() {
            // given
            List<String> nonexistentIds = Arrays.asList("nonexistent1", "nonexistent2");
            
            when(experimentService.getExperimentById("nonexistent1"))
                    .thenThrow(new RuntimeException("实验未找到"));
            when(experimentService.getExperimentById("nonexistent2"))
                    .thenThrow(new RuntimeException("实验未找到"));

            // when
            Map<String, Boolean> result = experimentInternalController.batchExperimentExists(nonexistentIds);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get("nonexistent1")).isFalse();
            assertThat(result.get("nonexistent2")).isFalse();

            verify(experimentService).getExperimentById("nonexistent1");
            verify(experimentService).getExperimentById("nonexistent2");
        }
    }

    @Nested
    @DisplayName("获取实验基本信息测试")
    class GetExperimentBasicInfoTests {

        @Test
        @DisplayName("成功获取实验基本信息")
        void shouldGetExperimentBasicInfoSuccessfully() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(sampleExperimentDTO);

            // when
            ExperimentDTO response = experimentInternalController.getExperimentBasicInfo("exp123");

            // then
            assertThat(response).isNotNull();
            assertThat(response).isEqualTo(sampleExperimentDTO);
            assertThat(response.getId()).isEqualTo("exp123");
            assertThat(response.getName()).isEqualTo("测试实验名称");

            verify(experimentService).getExperimentById("exp123");
        }

        @Test
        @DisplayName("获取不存在实验的基本信息")
        void shouldHandleGetNonexistentExperimentBasicInfo() {
            // given
            when(experimentService.getExperimentById("nonexistent"))
                    .thenThrow(new RuntimeException("实验未找到"));

            // when & then
            try {
                experimentInternalController.getExperimentBasicInfo("nonexistent");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("实验未找到");
            }

            verify(experimentService).getExperimentById("nonexistent");
        }

        @Test
        @DisplayName("获取实验基本信息时服务异常")
        void shouldHandleServiceExceptionWhenGettingBasicInfo() {
            // given
            when(experimentService.getExperimentById("exp123"))
                    .thenThrow(new RuntimeException("服务异常"));

            // when & then
            try {
                experimentInternalController.getExperimentBasicInfo("exp123");
            } catch (RuntimeException e) {
                assertThat(e.getMessage()).isEqualTo("服务异常");
            }

            verify(experimentService).getExperimentById("exp123");
        }

        @Test
        @DisplayName("获取实验基本信息时返回null")
        void shouldHandleNullBasicInfo() {
            // given
            when(experimentService.getExperimentById("exp123")).thenReturn(null);

            // when
            ExperimentDTO response = experimentInternalController.getExperimentBasicInfo("exp123");

            // then
            assertThat(response).isNull();

            verify(experimentService).getExperimentById("exp123");
        }
    }
}
