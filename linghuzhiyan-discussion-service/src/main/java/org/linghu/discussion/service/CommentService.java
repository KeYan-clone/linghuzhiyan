package org.linghu.discussion.service;

import org.linghu.discussion.dto.CommentRequestDTO;
import org.linghu.discussion.dto.CommentResponseDTO;
import org.linghu.discussion.dto.ReportRequestDTO;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 评论服务接口
 */
public interface CommentService {

    /**
     * 创建评论
     */
    CommentResponseDTO createComment(String discussionId, CommentRequestDTO requestDTO, String userId);

    /**
     * 获取讨论的评论列表
     */
    Page<CommentResponseDTO> getCommentsByDiscussionId(
            String discussionId,
            boolean rootOnly,
            String sortBy,
            String order,
            int page,
            int size,
            String currentUserId);

    /**
     * 获取评论的回复列表
     */
    List<CommentResponseDTO> getRepliesByCommentId(String commentId, String currentUserId);

    /**
     * 获取用户的评论列表
     */
    Page<CommentResponseDTO> getCommentsByUserId(String userId, int page, int size, String currentUserId);

    /**
     * 获取评论详情
     */
    CommentResponseDTO getCommentById(String commentId, String currentUserId);

    /**
     * 删除评论
     */
    boolean deleteComment(String commentId, String userId);

    /**
     * 点赞/取消点赞评论
     */
    CommentResponseDTO toggleLike(String commentId, String userId);

    /**
     * 举报评论
     */
    boolean reportComment(String commentId, ReportRequestDTO requestDTO, String reporterId);

    /**
     * 审核评论（管理员）
     */
    CommentResponseDTO reviewComment(String commentId, String status, String reviewerId);

    /**
     * 获取用户评论统计
     */
    long getUserCommentCount(String userId);

    /**
     * 获取讨论评论统计
     */
    long getDiscussionCommentCount(String discussionId);

    /**
     * 搜索评论
     */
    Page<CommentResponseDTO> searchComments(String keyword, int page, int size, String currentUserId);

    /**
     * 获取需要审核的评论
     */
    Page<CommentResponseDTO> getReportedComments(int page, int size);

    /**
     * 获取热门评论
     */
    List<CommentResponseDTO> getPopularComments(String discussionId, int limit, String currentUserId);
}
