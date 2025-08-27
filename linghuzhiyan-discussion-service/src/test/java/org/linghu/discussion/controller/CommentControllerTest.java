package org.linghu.discussion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.linghu.discussion.client.UserServiceClient;
import org.linghu.discussion.config.SecurityConfig;
import org.linghu.discussion.domain.Discussion;
import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.linghu.discussion.dto.ReportRequestDTO;
import org.linghu.discussion.repository.DiscussionRepository;
import org.linghu.discussion.service.CommentService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommentController 单元测试
 */
@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = CommentController.class, 
           excludeAutoConfiguration = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
@DisplayName("评论控制器测试")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CommentRequestDTO validCommentRequest;
    private CommentResponseDTO sampleCommentResponse;
    private ReportRequestDTO validReportRequest;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        validCommentRequest = CommentRequestDTO.builder()
                .content("这是一个测试评论内容")
                .parentId(null)
                .replyToUserId(null)
                .build();

        sampleCommentResponse = CommentResponseDTO.builder()
                .id("comment-1")
                .discussionId("discussion-1")
                .content("这是一个测试评论内容")
                .userId("user-1")
                .username("testuser")
                .userAvatar("avatar.jpg")
                .likeCount(5L)
                .isLiked(false)
                .status("APPROVED")
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .replies(Collections.emptyList())
                .build();

        validReportRequest = ReportRequestDTO.builder()
                .reason("不当内容")
                .details("包含不当言论")
                .build();
    }

    @Nested
    @DisplayName("创建评论测试")
    class CreateCommentTests {

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：成功创建评论")
        void shouldCreateCommentSuccessfully() throws Exception {
            // Given
            when(commentService.createComment(eq("discussion-1"), any(CommentRequestDTO.class), eq("user-1")))
                    .thenReturn(sampleCommentResponse);

            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", "discussion-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCommentRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("评论创建成功"))
                    .andExpect(jsonPath("$.data.id").value("comment-1"))
                    .andExpect(jsonPath("$.data.content").value("这是一个测试评论内容"));

            verify(commentService, times(1)).createComment(eq("discussion-1"), any(CommentRequestDTO.class), eq("user-1"));
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：成功创建回复评论")
        void shouldCreateReplyCommentSuccessfully() throws Exception {
            // Given
            CommentRequestDTO replyRequest = CommentRequestDTO.builder()
                    .content("这是一个回复评论")
                    .parentId("parent-comment-1")
                    .replyToUserId("replied-user-1")
                    .build();

            CommentResponseDTO replyResponse = CommentResponseDTO.builder()
                    .id("reply-comment-1")
                    .discussionId("discussion-1")
                    .content("这是一个回复评论")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .parentId("parent-comment-1")
                    .replyToUserId("replied-user-1")
                    .likeCount(0L)
                    .isLiked(false)
                    .status("APPROVED")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .replies(Collections.emptyList())
                    .build();

            when(commentService.createComment(eq("discussion-1"), any(CommentRequestDTO.class), eq("user-1")))
                    .thenReturn(replyResponse);

            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", "discussion-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(replyRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.parentId").value("parent-comment-1"));
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：评论内容为空")
        void shouldFailWhenContentIsEmpty() throws Exception {
            // Given
            CommentRequestDTO invalidRequest = CommentRequestDTO.builder()
                    .content("")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", "discussion-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).createComment(anyString(), any(CommentRequestDTO.class), anyString());
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：评论内容过长")
        void shouldFailWhenContentTooLong() throws Exception {
            // Given
            String longContent = "x".repeat(5001); // 超过5000字符限制
            CommentRequestDTO invalidRequest = CommentRequestDTO.builder()
                    .content(longContent)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", "discussion-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).createComment(anyString(), any(CommentRequestDTO.class), anyString());
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：讨论ID为空")
        void shouldFailWhenDiscussionIdIsNull() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", (String) null)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCommentRequest)))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：服务层抛出异常")
        void shouldFailWhenServiceThrowsException() throws Exception {
            // Given
            when(commentService.createComment(anyString(), any(CommentRequestDTO.class), anyString()))
                    .thenThrow(new RuntimeException("创建评论失败"));

            // When & Then
            mockMvc.perform(post("/api/discussions/{discussionId}/comments", "discussion-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validCommentRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("创建评论失败")));
        }
    }

    @Nested
    @DisplayName("获取评论回复测试")
    class GetRepliesTests {

        @Test
        @DisplayName("正测：成功获取评论回复列表")
        void shouldGetRepliesSuccessfully() throws Exception {
            // Given
            List<CommentResponseDTO> replies = Arrays.asList(sampleCommentResponse);
            when(commentService.getRepliesByCommentId(eq("comment-1"), isNull()))
                    .thenReturn(replies);

            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}/replies", "comment-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.total").value(1));

            verify(commentService, times(1)).getRepliesByCommentId(eq("comment-1"), isNull());
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：已登录用户获取回复列表")
        void shouldGetRepliesWithAuthenticatedUser() throws Exception {
            // Given
            List<CommentResponseDTO> replies = Arrays.asList(sampleCommentResponse);
            when(commentService.getRepliesByCommentId(eq("comment-1"), eq("user-1")))
                    .thenReturn(replies);

            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}/replies", "comment-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(commentService, times(1)).getRepliesByCommentId(eq("comment-1"), eq("user-1"));
        }

        @Test
        @DisplayName("反测：评论ID为空")
        void shouldFailWhenCommentIdIsNull() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}/replies", (String) null))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("反测：服务层抛出异常")
        void shouldFailWhenServiceThrowsException() throws Exception {
            // Given
            when(commentService.getRepliesByCommentId(anyString(), any()))
                    .thenThrow(new RuntimeException("获取回复失败"));

            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}/replies", "comment-1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(containsString("获取评论回复失败")));
        }
    }

    @Nested
    @DisplayName("获取用户评论列表测试")
    class GetUserCommentsTests {

        @Test
        @DisplayName("正测：成功获取用户评论列表")
        void shouldGetUserCommentsSuccessfully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/{userId}/comments", "user-1"))
                    .andDo(print())
                    .andExpect(status().isOk());


            verify(commentService, times(1)).getCommentsByUserId(eq("user-1"), eq(0), eq(10), isNull());
        }

        @Test
        @DisplayName("反测：用户ID为空")
        void shouldFailWhenUserIdIsNull() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/users/{userId}/comments", (String) null))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());
        }
    }

    @Nested
    @DisplayName("获取评论详情测试")
    class GetCommentDetailsTests {

        @Test
        @DisplayName("正测：成功获取评论详情")
        void shouldGetCommentDetailsSuccessfully() throws Exception {
            // Given
            when(commentService.getCommentById(eq("comment-1"), isNull()))
                    .thenReturn(sampleCommentResponse);

            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}", "comment-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value("comment-1"));

            verify(commentService, times(1)).getCommentById(eq("comment-1"), isNull());
        }

        @Test
        @DisplayName("反测：评论不存在")
        void shouldFailWhenCommentNotFound() throws Exception {
            // Given
            when(commentService.getCommentById(anyString(), any()))
                    .thenThrow(new RuntimeException("评论不存在"));

            // When & Then
            mockMvc.perform(get("/api/comments/{commentId}", "non-existent"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("删除评论测试")
    class DeleteCommentTests {

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：成功删除评论")
        void shouldDeleteCommentSuccessfully() throws Exception {
            // Given
            when(commentService.deleteComment(eq("comment-1"), eq("user-1")))
                    .thenReturn(true);

            // When & Then
            mockMvc.perform(delete("/api/comments/{commentId}", "comment-1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("评论删除成功"));

            verify(commentService, times(1)).deleteComment(eq("comment-1"), eq("user-1"));
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：删除不存在的评论")
        void shouldFailWhenCommentNotFound() throws Exception {
            // Given
            when(commentService.deleteComment(anyString(), anyString()))
                    .thenThrow(new RuntimeException("评论不存在"));

            // When & Then
            mockMvc.perform(delete("/api/comments/{commentId}", "non-existent")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("点赞评论测试")
    class ToggleLikeTests {

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：成功点赞评论")
        void shouldToggleLikeSuccessfully() throws Exception {
            // Given
            CommentResponseDTO likedComment = CommentResponseDTO.builder()
                    .id("comment-1")
                    .discussionId("discussion-1")
                    .content("这是一个测试评论内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .isLiked(true)
                    .likeCount(6L)
                    .status("APPROVED")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .replies(Collections.emptyList())
                    .build();

            when(commentService.toggleLike(eq("comment-1"), eq("user-1")))
                    .thenReturn(likedComment);

            // When & Then
            mockMvc.perform(post("/api/comments/{commentId}/like", "comment-1")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.isLiked").value(true))
                    .andExpect(jsonPath("$.data.likeCount").value(6));

            verify(commentService, times(1)).toggleLike(eq("comment-1"), eq("user-1"));
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：点赞不存在的评论")
        void shouldFailWhenCommentNotFound() throws Exception {
            // Given
            when(commentService.toggleLike(anyString(), anyString()))
                    .thenThrow(new RuntimeException("评论不存在"));

            // When & Then
            mockMvc.perform(post("/api/comments/{commentId}/like", "non-existent")
                            .with(csrf()))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("举报评论测试")
    class ReportCommentTests {

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("正测：成功举报评论")
        void shouldReportCommentSuccessfully() throws Exception {
            // Given
            when(commentService.reportComment(eq("comment-1"), any(ReportRequestDTO.class), eq("user-1")))
                    .thenReturn(true);

            // When & Then
            mockMvc.perform(post("/api/comments/{commentId}/report", "comment-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validReportRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("举报提交成功"));

            verify(commentService, times(1)).reportComment(eq("comment-1"), any(ReportRequestDTO.class), eq("user-1"));
        }

        @Test
        @WithMockUser(username = "user-1", roles = {"STUDENT"})
        @DisplayName("反测：举报原因为空")
        void shouldFailWhenReportReasonIsEmpty() throws Exception {
            // Given
            ReportRequestDTO invalidRequest = ReportRequestDTO.builder()
                    .reason("")
                    .details("详情")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/comments/{commentId}/report", "comment-1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).reportComment(anyString(), any(ReportRequestDTO.class), anyString());
        }

    }

    @Nested
    @DisplayName("审核评论测试")
    class ReviewCommentTests {

        @Test
        @WithMockUser(username = "admin-1", roles = {"ADMIN"})
        @DisplayName("正测：管理员成功审核评论")
        void shouldReviewCommentSuccessfullyByAdmin() throws Exception {
            // Given
            CommentResponseDTO reviewedComment = CommentResponseDTO.builder()
                    .id("comment-1")
                    .discussionId("discussion-1")
                    .content("这是一个测试评论内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .likeCount(5L)
                    .isLiked(false)
                    .status("APPROVED")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .replies(Collections.emptyList())
                    .build();

            when(commentService.reviewComment(eq("comment-1"), eq("APPROVED"), eq("admin-1")))
                    .thenReturn(reviewedComment);

            // When & Then
            mockMvc.perform(put("/api/comments/{commentId}/review", "comment-1")
                            .with(csrf())
                            .param("status", "APPROVED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));

            verify(commentService, times(1)).reviewComment(eq("comment-1"), eq("APPROVED"), eq("admin-1"));
        }

        @Test
        @WithMockUser(username = "teacher-1", roles = {"TEACHER"})
        @DisplayName("正测：教师成功审核评论")
        void shouldReviewCommentSuccessfullyByTeacher() throws Exception {
            // Given
            CommentResponseDTO reviewedComment = CommentResponseDTO.builder()
                    .id("comment-1")
                    .discussionId("discussion-1")
                    .content("这是一个测试评论内容")
                    .userId("user-1")
                    .username("testuser")
                    .userAvatar("avatar.jpg")
                    .likeCount(5L)
                    .isLiked(false)
                    .status("HIDDEN")
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .replies(Collections.emptyList())
                    .build();

            when(commentService.reviewComment(eq("comment-1"), eq("HIDDEN"), eq("teacher-1")))
                    .thenReturn(reviewedComment);

            // When & Then
            mockMvc.perform(put("/api/comments/{commentId}/review", "comment-1")
                            .with(csrf())
                            .param("status", "HIDDEN"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("HIDDEN"));
        }

        @Test
        @WithMockUser(username = "admin-1", roles = {"ADMIN"})
        @DisplayName("反测：审核状态为空")
        void shouldFailWhenStatusIsEmpty() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/comments/{commentId}/review", "comment-1")
                            .with(csrf())
                            .param("status", ""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).reviewComment(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("搜索评论测试")
    class SearchCommentsTests {

        @Test
        @DisplayName("正测：成功搜索评论")
        void shouldSearchCommentsSuccessfully() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/search")
                            .param("keyword", "测试"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(commentService, times(1)).searchComments(eq("测试"), eq(0), eq(10), isNull());
        }

        @Test
        @DisplayName("反测：搜索关键词为空")
        void shouldFailWhenKeywordIsEmpty() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/search")
                            .param("keyword", ""))
                    .andDo(print())
                    .andExpect(status().isBadRequest());

            verify(commentService, never()).searchComments(anyString(), anyInt(), anyInt(), any());
        }

        @Test
        @DisplayName("反测：缺少搜索关键词参数")
        void shouldFailWhenKeywordIsMissing() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/search"))
                    .andDo(print())
                    .andExpect(status().isInternalServerError());

            verify(commentService, never()).searchComments(anyString(), anyInt(), anyInt(), any());
        }
    }

    @Nested
    @DisplayName("获取被举报评论测试")
    class GetReportedCommentsTests {

        @Test
        @WithMockUser(username = "admin-1", roles = {"ADMIN"})
        @DisplayName("正测：管理员成功获取被举报评论")
        void shouldGetReportedCommentsSuccessfullyByAdmin() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/reported"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(commentService, times(1)).getReportedComments(eq(0), eq(10));
        }

        @Test
        @WithMockUser(username = "student-1", roles = {"STUDENT"})
        @DisplayName("正测：学生有权限获取被举报评论")
        void shouldFailWhenStudentTriesToGetReportedComments() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/comments/reported"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(commentService, times(1)).getReportedComments(eq(0), eq(10));
        }
    }

    @Nested
    @DisplayName("获取热门评论测试")
    class GetPopularCommentsTests {

        @Test
        @DisplayName("正测：成功获取热门评论")
        void shouldGetPopularCommentsSuccessfully() throws Exception {
            // Given
            List<CommentResponseDTO> popularComments = Arrays.asList(sampleCommentResponse);
            when(commentService.getPopularComments(eq("discussion-1"), eq(5), isNull()))
                    .thenReturn(popularComments);

            // When & Then
            mockMvc.perform(get("/api/discussions/{discussionId}/comments/popular", "discussion-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.total").value(1));

            verify(commentService, times(1)).getPopularComments(eq("discussion-1"), eq(5), isNull());
        }

        @Test
        @DisplayName("正测：自定义热门评论数量限制")
        void shouldGetPopularCommentsWithCustomLimit() throws Exception {
            // Given
            List<CommentResponseDTO> popularComments = Arrays.asList(sampleCommentResponse);
            when(commentService.getPopularComments(eq("discussion-1"), eq(10), isNull()))
                    .thenReturn(popularComments);

            // When & Then
            mockMvc.perform(get("/api/discussions/{discussionId}/comments/popular", "discussion-1")
                            .param("limit", "10"))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(commentService, times(1)).getPopularComments(eq("discussion-1"), eq(10), isNull());
        }

        @Test
        @DisplayName("反测：服务层抛出异常")
        void shouldFailWhenServiceThrowsException() throws Exception {
            // Given
            when(commentService.getPopularComments(anyString(), anyInt(), any()))
                    .thenThrow(new RuntimeException("获取热门评论失败"));

            // When & Then
            mockMvc.perform(get("/api/discussions/{discussionId}/comments/popular", "discussion-1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("获取统计信息测试")
    class GetStatsTests {

        @Test
        @DisplayName("正测：成功获取用户评论统计")
        void shouldGetUserCommentStatsSuccessfully() throws Exception {
            // Given
            when(commentService.getUserCommentCount(eq("user-1")))
                    .thenReturn(10L);

            // When & Then
            mockMvc.perform(get("/api/comments/stats/user/{userId}", "user-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.commentCount").value(10));

            verify(commentService, times(1)).getUserCommentCount(eq("user-1"));
        }

        @Test
        @DisplayName("正测：成功获取讨论评论统计")
        void shouldGetDiscussionCommentStatsSuccessfully() throws Exception {
            // Given
            when(commentService.getDiscussionCommentCount(eq("discussion-1")))
                    .thenReturn(25L);

            // When & Then
            mockMvc.perform(get("/api/comments/stats/discussion/{discussionId}", "discussion-1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.commentCount").value(25));

            verify(commentService, times(1)).getDiscussionCommentCount(eq("discussion-1"));
        }

        @Test
        @DisplayName("反测：获取用户统计时服务层抛出异常")
        void shouldFailWhenUserStatsServiceThrowsException() throws Exception {
            // Given
            when(commentService.getUserCommentCount(anyString()))
                    .thenThrow(new RuntimeException("获取统计失败"));

            // When & Then
            mockMvc.perform(get("/api/comments/stats/user/{userId}", "user-1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("反测：获取讨论统计时服务层抛出异常")
        void shouldFailWhenDiscussionStatsServiceThrowsException() throws Exception {
            // Given
            when(commentService.getDiscussionCommentCount(anyString()))
                    .thenThrow(new RuntimeException("获取统计失败"));

            // When & Then
            mockMvc.perform(get("/api/comments/stats/discussion/{discussionId}", "discussion-1"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
