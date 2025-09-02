package org.linghu.discussion.controller;

import org.junit.jupiter.api.Test;
import org.linghu.discussion.config.JwtAuthenticationFilter;
import org.linghu.discussion.config.JwtTokenProvider;
import org.linghu.discussion.config.TestSecurityConfig;
import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.linghu.discussion.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureWebMvc
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testCreateComment() throws Exception {
        CommentResponseDTO responseDTO = new CommentResponseDTO();
        when(commentService.createComment(anyString(), any(CommentRequestDTO.class), anyString()))
                .thenReturn(responseDTO);

        mockMvc.perform(post("/api/comments/discussions/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"test comment\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCommentsByDiscussionId() throws Exception {
        when(commentService.getCommentsByDiscussionId(anyString(), anyBoolean(), anyString(), anyString(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(new CommentResponseDTO()), PageRequest.of(0,10), 1));

        mockMvc.perform(get("/api/comments/discussions/1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetRepliesByCommentId() throws Exception {
        when(commentService.getRepliesByCommentId(anyString(), any()))
                .thenReturn(List.of(new CommentResponseDTO()));

        mockMvc.perform(get("/api/comments/comments/1/replies"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCommentsByUserId() throws Exception {
        when(commentService.getCommentsByUserId(anyString(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(new CommentResponseDTO()), PageRequest.of(0,10), 1));

        mockMvc.perform(get("/api/comments/users/user1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetCommentById() throws Exception {
        when(commentService.getCommentById(anyString(), any())).thenReturn(new CommentResponseDTO());

        mockMvc.perform(get("/api/comments/comments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/comments/comments/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testToggleLike() throws Exception {
        when(commentService.toggleLike(anyString(), anyString()))
                .thenReturn(new CommentResponseDTO());

        mockMvc.perform(post("/api/comments/comments/1/like"))
                .andExpect(status().isOk());
    }

    @Test
    void testReportComment() throws Exception {
        mockMvc.perform(post("/api/comments/comments/1/report")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"spam\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void testReviewComment() throws Exception {
        when(commentService.reviewComment(anyString(), anyString(), anyString()))
                .thenReturn(new CommentResponseDTO());

        mockMvc.perform(put("/api/comments/comments/1/review")
                        .param("status", "APPROVED"))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchComments() throws Exception {
        when(commentService.searchComments(anyString(), anyInt(), anyInt(), any()))
                .thenReturn(new PageImpl<>(List.of(new CommentResponseDTO()), PageRequest.of(0,10), 1));

        mockMvc.perform(get("/api/comments/comments/search")
                        .param("keyword", "test"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetReportedComments() throws Exception {
        when(commentService.getReportedComments(anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(new CommentResponseDTO()), PageRequest.of(0,10), 1));

        mockMvc.perform(get("/api/comments/comments/reported"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetPopularComments() throws Exception {
        when(commentService.getPopularComments(anyString(), anyInt(), any()))
                .thenReturn(List.of(new CommentResponseDTO()));

        mockMvc.perform(get("/api/comments/discussions/1/comments/popular"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetUserCommentStats() throws Exception {
        when(commentService.getUserCommentCount(anyString())).thenReturn(5L);

        mockMvc.perform(get("/api/comments/stats/user/user1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetDiscussionCommentStats() throws Exception {
        when(commentService.getDiscussionCommentCount(anyString())).thenReturn(10L);

        mockMvc.perform(get("/api/comments/stats/discussion/1"))
                .andExpect(status().isOk());
    }
}
