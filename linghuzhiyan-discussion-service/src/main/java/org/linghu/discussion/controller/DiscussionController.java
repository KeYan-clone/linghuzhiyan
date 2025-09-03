package org.linghu.discussion.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.discussion.dto.*;
import org.linghu.discussion.service.DiscussionService;
import org.linghu.discussion.service.CommentService;
import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * 讨论管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@Validated
public class DiscussionController {

    private final DiscussionService discussionService;
    private final CommentService commentService;

    /**
     * 创建讨论
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> createDiscussion(@Valid @RequestBody DiscussionRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            DiscussionResponseDTO responseDTO = discussionService.createDiscussion(requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "讨论创建成功");
            response.put("data", responseDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建讨论失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建讨论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建评论（兼容路径）
     * 说明：原生评论创建接口在 CommentController 下为 /api/comments/discussions/{discussionId}/comments。
     * 为兼容客户端直接对 /api/discussions/{id}/comments 发起 POST，这里做一个转发委托。
     */
    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> createCommentCompat(
            @PathVariable @NotNull String id,
            @Valid @RequestBody CommentRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            CommentResponseDTO responseDTO = commentService.createComment(id, requestDTO, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "评论创建成功");
            response.put("data", responseDTO);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建评论失败(兼容路径): id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建评论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取讨论的评论列表（兼容路径）
     * 说明：原生为 CommentController 下的 GET /api/comments/discussions/{discussionId}/comments。
     * 为兼容客户端直接访问 /api/discussions/{id}/comments，这里做一个委托。
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<Page<CommentResponseDTO>> getCommentsByDiscussionIdCompat(
            @PathVariable @NotNull String id,
            @RequestParam(required = false, defaultValue = "false") boolean rootOnly,
            @RequestParam(required = false, defaultValue = "createTime") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户也允许浏览评论
        }

        Page<CommentResponseDTO> comments = commentService.getCommentsByDiscussionId(
                id, rootOnly, sortBy, order, page, size, currentUserId);

        return ResponseEntity.ok(comments);
    }

    /**
     * 获取讨论列表
     */
    @GetMapping
    public ResponseEntity<Page<DiscussionResponseDTO>> getDiscussions(
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String experimentId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "lastActivityTime") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览讨论
        }

        String[] tagArray = tags != null ? tags.split(",") : null;

        Page<DiscussionResponseDTO> discussions = discussionService.getDiscussions(
                tagArray, experimentId, userId, status, keyword, sortBy, order, page, size, currentUserId);

        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取讨论详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDiscussionById(@PathVariable @NotNull String id) {
        try {
            String currentUserId = null;
            try {
                currentUserId = getCurrentUserId();
            } catch (Exception e) {
                // 未登录用户不影响浏览讨论
            }

            DiscussionResponseDTO discussion = discussionService.getDiscussionById(id, currentUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", discussion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取讨论详情失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取讨论详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新讨论
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> updateDiscussion(
            @PathVariable @NotNull String id,
            @Valid @RequestBody DiscussionRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            DiscussionResponseDTO updatedDiscussion = discussionService.updateDiscussion(id, requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "讨论更新成功");
            response.put("data", updatedDiscussion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新讨论失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新讨论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除讨论
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> deleteDiscussion(@PathVariable @NotNull String id) {
        try {
            String currentUser = getCurrentUserId();
            discussionService.deleteDiscussion(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "讨论删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除讨论失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除讨论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 审核讨论
     */
    @PutMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> reviewDiscussion(
            @PathVariable @NotNull String id,
            @Valid @RequestBody ReviewRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            DiscussionResponseDTO reviewedDiscussion = discussionService.reviewDiscussion(id, requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "讨论审核完成");
            response.put("data", reviewedDiscussion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("审核讨论失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "审核讨论失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新讨论优先级
     */
    @PutMapping("/{id}/priority")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePriority(
            @PathVariable @NotNull String id,
            @Valid @RequestBody PriorityRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            DiscussionResponseDTO updatedDiscussion = discussionService.updatePriority(id, requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "优先级更新成功");
            response.put("data", updatedDiscussion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新优先级失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新优先级失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 点赞/取消点赞讨论
     */
    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> toggleLike(@PathVariable @NotNull String id) {
        try {
            String currentUser = getCurrentUserId();
            DiscussionResponseDTO updatedDiscussion = discussionService.toggleLike(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "操作成功");
            response.put("data", updatedDiscussion);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("点赞操作失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "点赞操作失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取热门讨论
     */
    @GetMapping("/popular")
    public ResponseEntity<Page<DiscussionResponseDTO>> getPopularDiscussions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览
        }

        Page<DiscussionResponseDTO> discussions = discussionService.getPopularDiscussions(page, size, currentUserId);
        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取最新活动讨论
     */
    @GetMapping("/recent-active")
    public ResponseEntity<Page<DiscussionResponseDTO>> getRecentActiveDiscussions(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览
        }

        Page<DiscussionResponseDTO> discussions = discussionService.getRecentActiveDiscussions(page, size, currentUserId);
        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取置顶讨论
     */
    @GetMapping("/pinned")
    public ResponseEntity<Page<DiscussionResponseDTO>> getPinnedDiscussions() {
        String currentUserId = null;
        try {
            currentUserId = getCurrentUserId();
        } catch (Exception e) {
            // 未登录用户不影响浏览
        }

        Page<DiscussionResponseDTO> discussions = discussionService.getPinnedDiscussions(currentUserId);
        return ResponseEntity.ok(discussions);
    }

    /**
     * 获取用户讨论统计
     */
    @GetMapping("/stats/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDiscussionStats(@PathVariable String userId) {
        try {
            long count = discussionService.getUserDiscussionCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("discussionCount", count));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取用户讨论统计失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取实验讨论统计
     */
    @GetMapping("/stats/experiment/{experimentId}")
    public ResponseEntity<Map<String, Object>> getExperimentDiscussionStats(@PathVariable String experimentId) {
        try {
            long count = discussionService.getExperimentDiscussionCount(experimentId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", Map.of("discussionCount", count));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取实验讨论统计失败: experimentId={}", experimentId, e);
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
