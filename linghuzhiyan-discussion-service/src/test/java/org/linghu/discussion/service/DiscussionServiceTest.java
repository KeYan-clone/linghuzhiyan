package org.linghu.discussion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.discussion.client.UserServiceClient;
import org.linghu.discussion.domain.Discussion;
import org.linghu.discussion.dto.*;
import org.linghu.discussion.repository.DiscussionRepository;
import org.linghu.discussion.service.impl.DiscussionServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DiscussionService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("讨论服务测试")
class DiscussionServiceTest {

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private DiscussionServiceImpl discussionService;

    private Discussion sampleDiscussion;
    private DiscussionRequestDTO discussionRequest;
    private ReviewRequestDTO reviewRequest;
    private PriorityRequestDTO priorityRequest;
    private UserServiceClient.UserInfo userInfo;

    @BeforeEach
    void setUp() {
        sampleDiscussion = Discussion.builder()
                .id("discussion-1")
                .title("测试讨论标题")
                .content("测试讨论内容")
                .userId("user-1")
                .username("testuser")
                .userAvatar("avatar.jpg")
                .tags(Arrays.asList("Java", "Spring"))
                .experimentId("experiment-1")
                .status(Discussion.DiscussionStatus.APPROVED)
                .priority(0)
                .viewCount(10L)
                .commentCount(5L)
                .likeCount(3L)
                .likedBy(new ArrayList<>())
                .lastActivityTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .deleted(false)
                .build();

        discussionRequest = DiscussionRequestDTO.builder()
                .title("新讨论标题")
                .content("新讨论内容")
                .tags(Arrays.asList("测试", "单元测试"))
                .experimentId("experiment-1")
                .build();

        reviewRequest = ReviewRequestDTO.builder()
                .status("APPROVED")
                .rejectionReason(null)
                .build();

        priorityRequest = PriorityRequestDTO.builder()
                .priority(5)
                .build();

        userInfo = UserServiceClient.UserInfo.builder()
                .id("user-1")
                .username("testuser")
                .email("test@example.com")
                .avatar("avatar.jpg")
                .department("计算机学院")
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("创建讨论测试")
    class CreateDiscussionTests {

        @Test
        @DisplayName("正测：成功创建讨论")
        void shouldCreateDiscussionSuccessfully() {
            // Given
            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);
            when(discussionRepository.save(any(Discussion.class))).thenReturn(sampleDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.createDiscussion(discussionRequest, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("discussion-1");
            assertThat(result.getTitle()).isEqualTo("测试讨论标题");
            assertThat(result.getContent()).isEqualTo("测试讨论内容");
            assertThat(result.getUserId()).isEqualTo("user-1");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getStatus()).isEqualTo("APPROVED");

            verify(userServiceClient, times(1)).getUserById("user-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：用户不存在")
        void shouldFailWhenUserNotFound() {
            // Given
            when(userServiceClient.getUserById("non-existent")).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> discussionService.createDiscussion(discussionRequest, "non-existent"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("用户不存在");

            verify(userServiceClient, times(1)).getUserById("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：标题为空")
        void shouldFailWhenTitleIsNull() {
            // Given
            DiscussionRequestDTO invalidRequest = DiscussionRequestDTO.builder()
                    .title(null)
                    .content("测试内容")
                    .build();

            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);

            // When & Then
            assertThatThrownBy(() -> discussionService.createDiscussion(invalidRequest, "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("创建讨论失败");

            verify(userServiceClient, times(1)).getUserById("user-1");
        }

        @Test
        @DisplayName("反测：内容为空")
        void shouldFailWhenContentIsNull() {
            // Given
            DiscussionRequestDTO invalidRequest = DiscussionRequestDTO.builder()
                    .title("测试标题")
                    .content(null)
                    .build();

            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);

            // When & Then
            assertThatThrownBy(() -> discussionService.createDiscussion(invalidRequest, "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("创建讨论失败");

            verify(userServiceClient, times(1)).getUserById("user-1");
        }
    }

    @Nested
    @DisplayName("获取讨论测试")
    class GetDiscussionTests {

        @Test
        @DisplayName("正测：成功根据ID获取讨论")
        void shouldGetDiscussionByIdSuccessfully() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            // Mock incrementViewCount调用
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));

            // When
            DiscussionResponseDTO result = discussionService.getDiscussionById("discussion-1", "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("discussion-1");
            assertThat(result.getTitle()).isEqualTo("测试讨论标题");
            // 验证基本信息而不是具体的浏览次数，因为incrementViewCount会修改它
            assertThat(result.getViewCount()).isNotNull();

            // 验证方法被调用了两次：一次获取讨论，一次用于增加浏览次数
            verify(discussionRepository, times(2)).findByIdAndNotDeleted("discussion-1");
        }

        @Test
        @DisplayName("反测：讨论不存在")
        void shouldFailWhenDiscussionNotFound() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.getDiscussionById("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("正测：成功获取讨论列表")
        void shouldGetDiscussionsSuccessfully() {
            // Given
            List<Discussion> discussions = Arrays.asList(sampleDiscussion);
            when(mongoTemplate.count(any(Query.class), eq(Discussion.class))).thenReturn(1L);
            when(mongoTemplate.find(any(Query.class), eq(Discussion.class))).thenReturn(discussions);

            // When - 指定status避免复杂的or查询条件
            Page<DiscussionResponseDTO> result = discussionService.getDiscussions(
                    null, null, null, "APPROVED", null, 
                    "lastActivityTime", "desc", 0, 10, "user-1"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("discussion-1");

            verify(mongoTemplate, times(1)).count(any(Query.class), eq(Discussion.class));
            verify(mongoTemplate, times(1)).find(any(Query.class), eq(Discussion.class));
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("正测：成功按标签过滤讨论")
        void shouldGetDiscussionsByTagsSuccessfully() {
            // Given
            String[] tags = {"Java", "Spring"};
            List<Discussion> discussions = Arrays.asList(sampleDiscussion);
            when(mongoTemplate.count(any(Query.class), eq(Discussion.class))).thenReturn(1L);
            when(mongoTemplate.find(any(Query.class), eq(Discussion.class))).thenReturn(discussions);

            // When - 指定status避免复杂的or查询条件
            Page<DiscussionResponseDTO> result = discussionService.getDiscussions(
                    tags, null, null, "APPROVED", null,
                    "lastActivityTime", "desc", 0, 10, "user-1"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTags()).contains("Java", "Spring");

            verify(mongoTemplate, times(1)).count(any(Query.class), eq(Discussion.class));
            verify(mongoTemplate, times(1)).find(any(Query.class), eq(Discussion.class));
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("正测：成功按关键词搜索讨论")
        void shouldSearchDiscussionsByKeywordSuccessfully() {
            // Given
            List<Discussion> discussions = Arrays.asList(sampleDiscussion);
            when(mongoTemplate.count(any(Query.class), eq(Discussion.class))).thenReturn(1L);
            when(mongoTemplate.find(any(Query.class), eq(Discussion.class))).thenReturn(discussions);

            // When - 指定status避免复杂的or查询条件组合
            Page<DiscussionResponseDTO> result = discussionService.getDiscussions(
                    null, null, null, "APPROVED", "测试",
                    "lastActivityTime", "desc", 0, 10, "user-1"
            );

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);

            verify(mongoTemplate, times(1)).count(any(Query.class), eq(Discussion.class));
            verify(mongoTemplate, times(1)).find(any(Query.class), eq(Discussion.class));
        }
    }

    @Nested
    @DisplayName("更新讨论测试")
    class UpdateDiscussionTests {

        @Test
        @DisplayName("正测：作者成功更新讨论")
        void shouldUpdateDiscussionSuccessfully() {
            // Given
            Discussion updatedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("更新后标题")
                    .content("更新后内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .status(Discussion.DiscussionStatus.PENDING)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .lastActivityTime(LocalDateTime.now())
                    .deleted(false)
                    .build();

            DiscussionRequestDTO updateRequest = DiscussionRequestDTO.builder()
                    .title("更新后标题")
                    .content("更新后内容")
                    .tags(Arrays.asList("更新", "测试"))
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(updatedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.updateDiscussion(
                    "discussion-1", updateRequest, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("更新后标题");
            assertThat(result.getContent()).isEqualTo("更新后内容");
            assertThat(result.getStatus()).isEqualTo("PENDING");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：非作者无权更新讨论")
        void shouldFailWhenNonAuthorTriesToUpdate() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));

            // When & Then
            assertThatThrownBy(() -> discussionService.updateDiscussion(
                    "discussion-1", discussionRequest, "other-user"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("无权限更新此讨论");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：更新不存在的讨论")
        void shouldFailWhenDiscussionNotFound() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.updateDiscussion(
                    "non-existent", discussionRequest, "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("删除讨论测试")
    class DeleteDiscussionTests {

        @Test
        @DisplayName("正测：作者成功删除讨论")
        void shouldDeleteDiscussionSuccessfully() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(sampleDiscussion);

            // When
            boolean result = discussionService.deleteDiscussion("discussion-1", "user-1");

            // Then
            assertThat(result).isTrue();

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：非作者无权删除讨论")
        void shouldFailWhenNonAuthorTriesToDelete() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));

            // When & Then
            assertThatThrownBy(() -> discussionService.deleteDiscussion("discussion-1", "other-user"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("无权限删除此讨论");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：删除不存在的讨论")
        void shouldFailWhenDiscussionNotFound() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.deleteDiscussion("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("审核讨论测试")
    class ReviewDiscussionTests {

        @Test
        @DisplayName("正测：成功审核通过讨论")
        void shouldApproveDiscussionSuccessfully() {
            // Given
            Discussion approvedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .status(Discussion.DiscussionStatus.APPROVED)
                    .approvedTime(LocalDateTime.now())
                    .rejectionReason(null)
                    .updateTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(approvedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.reviewDiscussion(
                    "discussion-1", reviewRequest, "admin-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("APPROVED");
            assertThat(result.getRejectionReason()).isNull();

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("正测：成功审核拒绝讨论")
        void shouldRejectDiscussionSuccessfully() {
            // Given
            ReviewRequestDTO rejectRequest = ReviewRequestDTO.builder()
                    .status("REJECTED")
                    .rejectionReason("内容不符合规范")
                    .build();

            Discussion rejectedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .status(Discussion.DiscussionStatus.REJECTED)
                    .rejectionReason("内容不符合规范")
                    .updateTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(rejectedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.reviewDiscussion(
                    "discussion-1", rejectRequest, "admin-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("REJECTED");
            assertThat(result.getRejectionReason()).isEqualTo("内容不符合规范");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：审核不存在的讨论")
        void shouldFailWhenReviewNonExistentDiscussion() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.reviewDiscussion(
                    "non-existent", reviewRequest, "admin-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("优先级管理测试")
    class PriorityManagementTests {

        @Test
        @DisplayName("正测：成功更新讨论优先级")
        void shouldUpdatePrioritySuccessfully() {
            // Given
            Discussion prioritizedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .priority(5)
                    .updateTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(prioritizedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.updatePriority(
                    "discussion-1", priorityRequest, "admin-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPriority()).isEqualTo(5);

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：更新不存在讨论的优先级")
        void shouldFailWhenUpdatePriorityOfNonExistentDiscussion() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.updatePriority(
                    "non-existent", priorityRequest, "admin-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("点赞功能测试")
    class ToggleLikeTests {

        @Test
        @DisplayName("正测：成功点赞讨论")
        void shouldToggleLikeSuccessfully() {
            // Given
            Discussion likedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .likeCount(4L)
                    .likedBy(Arrays.asList("user-2"))
                    .updateTime(LocalDateTime.now())
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(likedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.toggleLike("discussion-1", "user-2");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLikeCount()).isEqualTo(4L);
            assertThat(result.getIsLiked()).isTrue();

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("正测：成功取消点赞讨论")
        void shouldToggleUnlikeSuccessfully() {
            // Given
            Discussion discussionWithUserLike = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .likeCount(3L)
                    .likedBy(new ArrayList<>(Arrays.asList("user-2")))
                    .updateTime(LocalDateTime.now())
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            Discussion unlikedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .likeCount(2L)
                    .likedBy(new ArrayList<>())
                    .updateTime(LocalDateTime.now())
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(discussionWithUserLike));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(unlikedDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.toggleLike("discussion-1", "user-2");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLikeCount()).isEqualTo(2L);
            assertThat(result.getIsLiked()).isFalse();

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：点赞不存在的讨论")
        void shouldFailWhenToggleLikeOnNonExistentDiscussion() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> discussionService.toggleLike("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("讨论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("浏览次数测试")
    class IncrementViewCountTests {

        @Test
        @DisplayName("正测：成功增加浏览次数")
        void shouldIncrementViewCountSuccessfully() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(sampleDiscussion);

            // When
            discussionService.incrementViewCount("discussion-1");

            // Then
            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("反测：增加不存在讨论的浏览次数（应该不抛异常）")
        void shouldNotFailWhenIncrementViewCountOfNonExistentDiscussion() {
            // Given
            when(discussionRepository.findByIdAndNotDeleted("non-existent"))
                    .thenReturn(Optional.empty());

            // When & Then - 不应该抛异常
            assertThatCode(() -> discussionService.incrementViewCount("non-existent"))
                    .doesNotThrowAnyException();

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }
    }

    @Nested
    @DisplayName("统计功能测试")
    class StatisticsTests {

        @Test
        @DisplayName("正测：成功获取用户讨论统计")
        void shouldGetUserDiscussionCountSuccessfully() {
            // Given
            when(discussionRepository.countByUserIdAndNotDeleted("user-1")).thenReturn(15L);

            // When
            long result = discussionService.getUserDiscussionCount("user-1");

            // Then
            assertThat(result).isEqualTo(15L);

            verify(discussionRepository, times(1)).countByUserIdAndNotDeleted("user-1");
        }

        @Test
        @DisplayName("正测：成功获取实验讨论统计")
        void shouldGetExperimentDiscussionCountSuccessfully() {
            // Given
            when(discussionRepository.countByExperimentIdAndNotDeleted("experiment-1")).thenReturn(8L);

            // When
            long result = discussionService.getExperimentDiscussionCount("experiment-1");

            // Then
            assertThat(result).isEqualTo(8L);

            verify(discussionRepository, times(1)).countByExperimentIdAndNotDeleted("experiment-1");
        }

        @Test
        @DisplayName("正测：用户无讨论时返回0")
        void shouldReturnZeroWhenUserHasNoDiscussions() {
            // Given
            when(discussionRepository.countByUserIdAndNotDeleted("user-no-discussions")).thenReturn(0L);

            // When
            long result = discussionService.getUserDiscussionCount("user-no-discussions");

            // Then
            assertThat(result).isEqualTo(0L);

            verify(discussionRepository, times(1)).countByUserIdAndNotDeleted("user-no-discussions");
        }
    }

    @Nested
    @DisplayName("热门和活跃讨论测试")
    class PopularAndActiveDiscussionTests {

        @Test
        @DisplayName("正测：成功获取热门讨论")
        void shouldGetPopularDiscussionsSuccessfully() {
            // Given
            List<Discussion> discussions = Arrays.asList(sampleDiscussion);
            Page<Discussion> discussionPage = new PageImpl<>(discussions);

            when(discussionRepository.findPopularDiscussions(any(Pageable.class)))
                    .thenReturn(discussionPage);

            // When
            Page<DiscussionResponseDTO> result = discussionService.getPopularDiscussions(0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("discussion-1");

            verify(discussionRepository, times(1)).findPopularDiscussions(any(Pageable.class));
        }

        @Test
        @DisplayName("正测：成功获取最新活动讨论")
        void shouldGetRecentActiveDiscussionsSuccessfully() {
            // Given
            List<Discussion> discussions = Arrays.asList(sampleDiscussion);
            Page<Discussion> discussionPage = new PageImpl<>(discussions);

            when(discussionRepository.findRecentActiveDiscussions(any(LocalDateTime.class), any(Pageable.class)))
                    .thenReturn(discussionPage);

            // When
            Page<DiscussionResponseDTO> result = discussionService.getRecentActiveDiscussions(0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("discussion-1");

            verify(discussionRepository, times(1)).findRecentActiveDiscussions(
                    any(LocalDateTime.class), any(Pageable.class));
        }

        @Test
        @DisplayName("正测：成功获取置顶讨论")
        void shouldGetPinnedDiscussionsSuccessfully() {
            // Given
            Discussion pinnedDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("置顶讨论")
                    .content("重要讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .priority(10)
                    .status(Discussion.DiscussionStatus.APPROVED)
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            List<Discussion> discussions = Arrays.asList(pinnedDiscussion);

            when(discussionRepository.findPinnedDiscussions()).thenReturn(discussions);

            // When
            Page<DiscussionResponseDTO> result = discussionService.getPinnedDiscussions("user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getPriority()).isEqualTo(10);

            verify(discussionRepository, times(1)).findPinnedDiscussions();
        }

        @Test
        @DisplayName("正测：无热门讨论时返回空页面")
        void shouldReturnEmptyPageWhenNoPopularDiscussions() {
            // Given
            Page<Discussion> emptyPage = new PageImpl<>(new ArrayList<>());

            when(discussionRepository.findPopularDiscussions(any(Pageable.class)))
                    .thenReturn(emptyPage);

            // When
            Page<DiscussionResponseDTO> result = discussionService.getPopularDiscussions(0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);

            verify(discussionRepository, times(1)).findPopularDiscussions(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("边界情况测试")
    class EdgeCaseTests {

        @Test
        @DisplayName("反测：使用无效的审核状态")
        void shouldFailWithInvalidReviewStatus() {
            // Given
            ReviewRequestDTO invalidRequest = ReviewRequestDTO.builder()
                    .status("INVALID_STATUS")
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));

            // When & Then
            assertThatThrownBy(() -> discussionService.reviewDiscussion(
                    "discussion-1", invalidRequest, "admin-1"))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, never()).save(any(Discussion.class));
        }

        @Test
        @DisplayName("正测：处理空标签列表")
        void shouldHandleEmptyTagsList() {
            // Given
            DiscussionRequestDTO requestWithEmptyTags = DiscussionRequestDTO.builder()
                    .title("测试标题")
                    .content("测试内容")
                    .tags(new ArrayList<>())
                    .build();

            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);
            when(discussionRepository.save(any(Discussion.class))).thenReturn(sampleDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.createDiscussion(requestWithEmptyTags, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("discussion-1");

            verify(userServiceClient, times(1)).getUserById("user-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }

        @Test
        @DisplayName("正测：处理负优先级")
        void shouldHandleNegativePriority() {
            // Given
            PriorityRequestDTO negativePriorityRequest = PriorityRequestDTO.builder()
                    .priority(-5)
                    .build();

            Discussion negPriorityDiscussion = Discussion.builder()
                    .id("discussion-1")
                    .title("测试讨论标题")
                    .content("测试讨论内容")
                    .userId("user-1")
                    .username("testuser")
                    .priority(-5)
                    .updateTime(LocalDateTime.now())
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(sampleDiscussion));
            when(discussionRepository.save(any(Discussion.class))).thenReturn(negPriorityDiscussion);

            // When
            DiscussionResponseDTO result = discussionService.updatePriority(
                    "discussion-1", negativePriorityRequest, "admin-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getPriority()).isEqualTo(-5);

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(discussionRepository, times(1)).save(any(Discussion.class));
        }
    }
}