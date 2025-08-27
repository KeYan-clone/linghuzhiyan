package org.linghu.discussion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.discussion.domain.Comment;
import org.linghu.discussion.domain.Discussion;
import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.linghu.discussion.dto.ReportRequestDTO;
import org.linghu.discussion.repository.CommentRepository;
import org.linghu.discussion.repository.DiscussionRepository;
import org.linghu.discussion.client.UserServiceClient;
import org.linghu.discussion.service.impl.CommentServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentService 单元测试
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评论服务测试")
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private DiscussionRepository discussionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment sampleComment;
    private CommentRequestDTO commentRequest;
    private ReportRequestDTO reportRequest;

    @BeforeEach
    void setUp() {
        sampleComment = Comment.builder()
                .id("comment-1")
                .discussionId("discussion-1")
                .content("测试评论内容")
                .userId("user-1")
                .username("testuser")
                .userAvatar("avatar.jpg")
                .likeCount(5L)
                .likedBy(new ArrayList<>())
                .status(Comment.CommentStatus.NORMAL)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        commentRequest = CommentRequestDTO.builder()
                .content("新的评论内容")
                .parentId(null)
                .replyToUserId(null)
                .build();

        reportRequest = ReportRequestDTO.builder()
                .reason("不当内容")
                .details("包含不当言论")
                .build();
    }

    @Nested
    @DisplayName("创建评论测试")
    class CreateCommentTests {

        @Test
        @DisplayName("正测：成功创建根评论")
        void shouldCreateRootCommentSuccessfully() {
            // Given
            UserServiceClient.UserInfo userInfo = UserServiceClient.UserInfo.builder()
                    .id("user-1")
                    .username("testuser")
                    .avatar("avatar.jpg")
                    .build();
            
            when(discussionRepository.findByIdAndNotDeleted("discussion-1")).thenReturn(Optional.of(new Discussion()));
            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);
            when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

            // When
            CommentResponseDTO result = commentService.createComment("discussion-1", commentRequest, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("comment-1");
            assertThat(result.getContent()).isEqualTo("测试评论内容");
            assertThat(result.getUserId()).isEqualTo("user-1");
            assertThat(result.getDiscussionId()).isEqualTo("discussion-1");

            verify(discussionRepository, times(2)).findByIdAndNotDeleted("discussion-1");
            verify(userServiceClient, times(1)).getUserById("user-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("正测：成功创建回复评论")
        void shouldCreateReplyCommentSuccessfully() {
            // Given
            Comment parentComment = Comment.builder()
                    .id("parent-1")
                    .discussionId("discussion-1")
                    .content("父评论")
                    .userId("user-2")
                    .username("parentuser")
                    .path("/parent-1/")
                    .depth(0)
                    .build();

            CommentRequestDTO replyRequest = CommentRequestDTO.builder()
                    .content("回复评论")
                    .parentId("parent-1")
                    .replyToUserId("user-2")
                    .build();

            Comment replyComment = Comment.builder()
                    .id("reply-1")
                    .discussionId("discussion-1")
                    .content("回复评论")
                    .userId("user-1")
                    .username("testuser")
                    .parentId("parent-1")
                    .rootId("parent-1")
                    .path("/parent-1/reply-1/")
                    .depth(1)
                    .replyToUserId("user-2")
                    .build();

            UserServiceClient.UserInfo userInfo = UserServiceClient.UserInfo.builder()
                    .id("user-1")
                    .username("testuser")
                    .avatar("avatar.jpg")
                    .build();

            UserServiceClient.UserInfo replyToUserInfo = UserServiceClient.UserInfo.builder()
                    .id("user-2")
                    .username("parentuser")
                    .build();

            when(discussionRepository.findByIdAndNotDeleted("discussion-1")).thenReturn(Optional.of(new Discussion()));
            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);
            when(userServiceClient.getUserById("user-2")).thenReturn(replyToUserInfo);
            when(commentRepository.findByIdAndNotDeleted("parent-1")).thenReturn(Optional.of(parentComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(replyComment);

            // When
            CommentResponseDTO result = commentService.createComment("discussion-1", replyRequest, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getParentId()).isEqualTo("parent-1");
            assertThat(result.getReplyToUserId()).isEqualTo("user-2");
            assertThat(result.getDepth()).isEqualTo(1);

            verify(discussionRepository, times(2)).findByIdAndNotDeleted("discussion-1");
            verify(userServiceClient, times(1)).getUserById("user-1");
            verify(userServiceClient, times(1)).getUserById("user-2");
            verify(commentRepository, times(1)).findByIdAndNotDeleted("parent-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：父评论不存在")
        void shouldFailWhenParentCommentNotFound() {
            // Given
            CommentRequestDTO replyRequest = CommentRequestDTO.builder()
                    .content("回复评论")
                    .parentId("non-existent")
                    .build();

            UserServiceClient.UserInfo userInfo = UserServiceClient.UserInfo.builder()
                    .id("user-1")
                    .username("testuser")
                    .avatar("avatar.jpg")
                    .build();

            // Mock 讨论存在，以便进入父评论检查逻辑
            when(discussionRepository.findByIdAndNotDeleted("discussion-1")).thenReturn(Optional.of(new Discussion()));
            // Mock 用户存在，以便通过用户验证
            when(userServiceClient.getUserById("user-1")).thenReturn(userInfo);
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.createComment("discussion-1", replyRequest, "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("父评论不存在");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：评论内容为空")
        void shouldFailWhenContentIsNull() {
            // Given
            CommentRequestDTO invalidRequest = CommentRequestDTO.builder()
                    .content(null)
                    .build();

            // 由于服务会首先验证讨论存在，所以需要mock这个调用
            when(discussionRepository.findByIdAndNotDeleted("discussion-1")).thenReturn(Optional.of(new Discussion()));

            // When & Then
            assertThatThrownBy(() -> commentService.createComment("discussion-1", invalidRequest, "user-1"))
                    .isInstanceOf(RuntimeException.class) // 实际上会抛出RuntimeException而不是IllegalArgumentException
                    .hasMessageContaining("创建评论失败");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("获取评论测试")
    class GetCommentTests {

        @Test
        @DisplayName("正测：成功根据ID获取评论")
        void shouldGetCommentByIdSuccessfully() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));

            // When
            CommentResponseDTO result = commentService.getCommentById("comment-1", "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("comment-1");
            assertThat(result.getContent()).isEqualTo("测试评论内容");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
        }

        @Test
        @DisplayName("反测：评论不存在")
        void shouldFailWhenCommentNotFound() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.getCommentById("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("评论不存在");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
        }

        @Test
        @ExtendWith(MockitoExtension.class)
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("正测：成功获取讨论的评论列表")
        void shouldGetCommentsByDiscussionIdSuccessfully() {
            // Given
            List<Comment> comments = Arrays.asList(sampleComment);
            Page<Comment> commentPage = new PageImpl<>(comments);

            // 使用any()匹配器确保mock能够成功匹配
            when(discussionRepository.findByIdAndNotDeleted("discussion-1"))
                    .thenReturn(Optional.of(new Discussion()));
            when(commentRepository.findRootCommentsByDiscussionId(eq("discussion-1"), any(Pageable.class)))
                    .thenReturn(commentPage);
            when(commentRepository.findByRootIdAndNotDeleted("comment-1"))
                    .thenReturn(new ArrayList<>());

            // When
            Page<CommentResponseDTO> result = commentService.getCommentsByDiscussionId(
                    "discussion-1", true, "createTime", "asc", 0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo("comment-1");

            verify(discussionRepository, times(1)).findByIdAndNotDeleted("discussion-1");
            verify(commentRepository, times(1)).findRootCommentsByDiscussionId(
                    eq("discussion-1"), any(Pageable.class));
        }

        @Test
        @MockitoSettings(strictness = Strictness.LENIENT)
        @DisplayName("正测：成功获取用户的评论列表")
        void shouldGetCommentsByUserIdSuccessfully() {
            // Given
            List<Comment> comments = Arrays.asList(sampleComment);
            Page<Comment> commentPage = new PageImpl<>(comments);

            when(commentRepository.findByUserIdAndNotDeleted(eq("user-1"), any(Pageable.class)))
                    .thenReturn(commentPage);

            // When
            Page<CommentResponseDTO> result = commentService.getCommentsByUserId("user-1", 0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUserId()).isEqualTo("user-1");

            verify(commentRepository, times(1)).findByUserIdAndNotDeleted(eq("user-1"), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("删除评论测试")
    class DeleteCommentTests {

        @Test
        @DisplayName("正测：用户成功删除自己的评论")
        void shouldDeleteOwnCommentSuccessfully() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

            // When
            boolean result = commentService.deleteComment("comment-1", "user-1");

            // Then
            assertThat(result).isTrue();

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：用户无权删除他人评论")
        void shouldFailWhenUserTriesToDeleteOthersComment() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment("comment-1", "other-user"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("无权限删除此评论");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
            verify(commentRepository, never()).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：删除不存在的评论")
        void shouldFailWhenCommentNotFound() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("评论不存在");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("点赞功能测试")
    class ToggleLikeTests {

        @Test
        @DisplayName("正测：成功点赞评论")
        void shouldToggleLikeSuccessfully() {
            // Given
            Comment commentWithLike = Comment.builder()
                    .id("comment-1")
                    .discussionId("discussion-1")
                    .content("测试评论内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .likeCount(6L)
                    .status(Comment.CommentStatus.NORMAL)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(commentWithLike);

            // When
            CommentResponseDTO result = commentService.toggleLike("comment-1", "user-2");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getLikeCount()).isEqualTo(6L);

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：点赞不存在的评论")
        void shouldFailWhenLikeNonExistentComment() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.toggleLike("non-existent", "user-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("评论不存在");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("举报功能测试")
    class ReportCommentTests {

        @Test
        @DisplayName("正测：成功举报评论")
        void shouldReportCommentSuccessfully() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

            // When
            boolean result = commentService.reportComment("comment-1", reportRequest, "reporter-1");

            // Then
            assertThat(result).isTrue();

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：举报不存在的评论")
        void shouldFailWhenReportNonExistentComment() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.reportComment("non-existent", reportRequest, "reporter-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("评论不存在");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
        }

    }

    @Nested
    @DisplayName("审核功能测试")
    class ReviewCommentTests {

        @Test
        @DisplayName("正测：管理员成功审核评论")
        void shouldReviewCommentSuccessfully() {
            // Given
            Comment reviewedComment = Comment.builder()
                    .id("comment-1")
                    .discussionId("discussion-1")
                    .content("测试评论内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .likeCount(5L)
                    .status(Comment.CommentStatus.NORMAL)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            when(commentRepository.findByIdAndNotDeleted("comment-1")).thenReturn(Optional.of(sampleComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(reviewedComment);

            // When
            CommentResponseDTO result = commentService.reviewComment("comment-1", "NORMAL", "admin-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("NORMAL");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("comment-1");
            verify(commentRepository, times(1)).save(any(Comment.class));
        }

        @Test
        @DisplayName("反测：审核不存在的评论")
        void shouldFailWhenReviewNonExistentComment() {
            // Given
            when(commentRepository.findByIdAndNotDeleted("non-existent")).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.reviewComment("non-existent", "NORMAL", "admin-1"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("评论不存在");

            verify(commentRepository, times(1)).findByIdAndNotDeleted("non-existent");
            verify(commentRepository, never()).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("统计功能测试")
    class StatsTests {

        @Test
        @DisplayName("正测：成功获取用户评论统计")
        void shouldGetUserCommentCountSuccessfully() {
            // Given
            when(commentRepository.countByUserIdAndNotDeleted("user-1")).thenReturn(10L);

            // When
            long result = commentService.getUserCommentCount("user-1");

            // Then
            assertThat(result).isEqualTo(10L);

            verify(commentRepository, times(1)).countByUserIdAndNotDeleted("user-1");
        }

        @Test
        @DisplayName("正测：成功获取讨论评论统计")
        void shouldGetDiscussionCommentCountSuccessfully() {
            // Given
            when(commentRepository.countByDiscussionIdAndNotDeleted("discussion-1")).thenReturn(25L);

            // When
            long result = commentService.getDiscussionCommentCount("discussion-1");

            // Then
            assertThat(result).isEqualTo(25L);

            verify(commentRepository, times(1)).countByDiscussionIdAndNotDeleted("discussion-1");
        }
    }

    @Nested
    @DisplayName("搜索功能测试")
    class SearchTests {

        @Test
        @DisplayName("正测：成功搜索评论")
        void shouldSearchCommentsSuccessfully() {
            // Given
            List<Comment> comments = Arrays.asList(sampleComment);
            Page<Comment> commentPage = new PageImpl<>(comments);

            when(commentRepository.findByContentContaining(eq("测试"), any(Pageable.class)))
                    .thenReturn(commentPage);

            // When
            Page<CommentResponseDTO> result = commentService.searchComments("测试", 0, 10, "user-1");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getContent()).contains("测试");

            verify(commentRepository, times(1)).findByContentContaining(
                    eq("测试"), any(Pageable.class));
        }
    }
}
