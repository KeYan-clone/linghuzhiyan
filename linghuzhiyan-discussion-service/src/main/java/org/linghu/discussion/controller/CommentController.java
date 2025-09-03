package org.linghu.discussion.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.linghu.discussion.dto.ReportRequestDTO;
import org.linghu.discussion.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评论管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentService commentService;

    /**
     * 创建评论
     */
    @PostMapping("/discussions/{discussionId}/comments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> createComment(
            @PathVariable @NotNull String discussionId,
            @Valid @RequestBody CommentRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            CommentResponseDTO responseDTO = commentService.createComment(discussionId, requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论创建成功");
            response.put("data", responseDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建评论失败: discussionId={}", discussionId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建评论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取讨论的评论列表
     */
    @GetMapping("/discussions/{discussionId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByDiscussionId(
            @PathVariable @NotNull String discussionId,
            @RequestParam(required = false, defaultValue = "false") boolean rootOnly,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览评论
        }

        Page<CommentResponseDTO> comments = commentService.getCommentsByDiscussionId(
                discussionId, rootOnly, sortBy, order, page, size, currentUserId);

        return ResponseEntity.ok(comments);
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping({"/comments/{commentId}/replies", "/{commentId}/replies"})
    public ResponseEntity<Map<String, Object>> getRepliesByCommentId(@PathVariable @NotNull String commentId) {
        try {
            String currentUserId = null;
            try {
                currentUserId = getCurrentUserId();
            } catch (Exception e) {
                // 未登录用户不影响浏览
            }

            List<CommentResponseDTO> replies = commentService.getRepliesByCommentId(commentId, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", replies);
            response.put("total", replies.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取评论回复失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取评论回复失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户的评论列表
     */
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByUserId(
            @PathVariable @NotNull String userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览
        }

        Page<CommentResponseDTO> comments = commentService.getCommentsByUserId(userId, page, size, currentUserId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 获取评论详情
     */
    @GetMapping({"/comments/{commentId}", "/{commentId}"})
    public ResponseEntity<Map<String, Object>> getCommentById(@PathVariable @NotNull String commentId) {
        try {
            String currentUserId = null;
            try {
                currentUserId = getCurrentUserId();
            } catch (Exception e) {
                // 未登录用户不影响浏览
            }

            CommentResponseDTO comment = commentService.getCommentById(commentId, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取评论详情失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取评论详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping({"/comments/{commentId}", "/{commentId}"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> deleteComment(@PathVariable @NotNull String commentId) {
        try {
            String currentUser = getCurrentUserId();
            commentService.deleteComment(commentId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除评论失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除评论失败: " + e.getMessage());
            
            // 如果是权限相关的错误，返回403状态码
            if (e.getMessage().contains("无权限删除此评论") || e.getMessage().contains("权限不足")) {
                return ResponseEntity.status(403).body(response);
            }
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 点赞/取消点赞评论
     */
    @PostMapping({"/comments/{commentId}/like", "/{commentId}/like"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable @NotNull String commentId) {
        try {
            String currentUser = getCurrentUserId();
            CommentResponseDTO updatedComment = commentService.toggleLike(commentId, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "操作成功");
            response.put("data", updatedComment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("点赞操作失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "点赞操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 举报评论
     */
    @PostMapping({"/comments/{commentId}/report", "/{commentId}/report"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> reportComment(
            @PathVariable @NotNull String commentId,
            @Valid @RequestBody ReportRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            commentService.reportComment(commentId, requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "举报提交成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("举报评论失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "举报失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 审核评论（管理员）
     */
    @PutMapping({"/comments/{commentId}/review", "/{commentId}/review"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> reviewComment(
            @PathVariable @NotNull String commentId,
            @RequestParam @NotEmpty String status) {
        try {
            String currentUser = getCurrentUserId();
            CommentResponseDTO reviewedComment = commentService.reviewComment(commentId, status, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论审核完成");
            response.put("data", reviewedComment);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("审核评论失败: commentId={}", commentId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审核失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 搜索评论
     */
    @GetMapping({"/comments/search", "/search"})
    public ResponseEntity<Page<CommentResponseDTO>> searchComments(
            @RequestParam @NotEmpty String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响搜索
        }

        Page<CommentResponseDTO> comments = commentService.searchComments(keyword, page, size, currentUserId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 获取需要审核的评论（管理员）
     */
    @GetMapping({"/comments/reported", "/reported"})
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Page<CommentResponseDTO>> getReportedComments(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        Page<CommentResponseDTO> comments = commentService.getReportedComments(page, size);
        return ResponseEntity.ok(comments);
    }

    /**
     * 获取热门评论
     */
    @GetMapping("/discussions/{discussionId}/comments/popular")
    public ResponseEntity<Map<String, Object>> getPopularComments(
            @PathVariable @NotNull String discussionId,
            @RequestParam(required = false, defaultValue = "5") int limit) {
        try {
            String currentUserId = null;
            try {
                currentUserId = getCurrentUserId();
            } catch (Exception e) {
                // 未登录用户不影响浏览
            }

            List<CommentResponseDTO> comments = commentService.getPopularComments(discussionId, limit, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", comments);
            response.put("total", comments.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取热门评论失败: discussionId={}", discussionId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取热门评论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户评论统计
     */
    @GetMapping({"/comments/stats/user/{userId}", "/stats/user/{userId}"})
    public ResponseEntity<Map<String, Object>> getUserCommentStats(@PathVariable String userId) {
        try {
            long count = commentService.getUserCommentCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("commentCount", count));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户评论统计失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取讨论评论统计
     */
    @GetMapping({"/comments/stats/discussion/{discussionId}", "/stats/discussion/{discussionId}"})
    public ResponseEntity<Map<String, Object>> getDiscussionCommentStats(@PathVariable String discussionId) {
        try {
            long count = commentService.getDiscussionCommentCount(discussionId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("commentCount", count));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取讨论评论统计失败: discussionId={}", discussionId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("用户未认证");
    }
}
