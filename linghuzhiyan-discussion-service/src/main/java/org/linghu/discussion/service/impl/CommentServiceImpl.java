package org.linghu.discussion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.discussion.client.UserServiceClient;
import org.linghu.discussion.domain.Comment;
import org.linghu.discussion.dto.*;
import org.linghu.discussion.repository.CommentRepository;
import org.linghu.discussion.repository.DiscussionRepository;
import org.linghu.discussion.service.CommentService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final DiscussionRepository discussionRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public CommentResponseDTO createComment(String discussionId, CommentRequestDTO requestDTO, String userId) {
        try {
            // 验证讨论是否存在
            discussionRepository.findByIdAndNotDeleted(discussionId)
                    .orElseThrow(() -> new RuntimeException("讨论不存在: " + discussionId));

            // 获取用户信息
            UserServiceClient.UserInfo userInfo = userServiceClient.getUserById(userId);
            if (userInfo == null) {
                throw new RuntimeException("用户不存在: " + userId);
            }

            // 构建评论路径和深度
            String path = "";
            String rootId = null;
            int depth = 0;

            if (StringUtils.hasText(requestDTO.getParentId())) {
                Comment parentComment = commentRepository.findByIdAndNotDeleted(requestDTO.getParentId())
                        .orElseThrow(() -> new RuntimeException("父评论不存在: " + requestDTO.getParentId()));
                
                rootId = StringUtils.hasText(parentComment.getRootId()) ? 
                        parentComment.getRootId() : parentComment.getId();
                depth = parentComment.getDepth() + 1;
                path = StringUtils.hasText(parentComment.getPath()) ?
                        parentComment.getPath() + "." + parentComment.getId() :
                        parentComment.getId();
            }

            // 获取回复目标用户信息
            String replyToUsername = null;
            if (StringUtils.hasText(requestDTO.getReplyToUserId())) {
                UserServiceClient.UserInfo replyToUser = userServiceClient.getUserById(requestDTO.getReplyToUserId());
                if (replyToUser != null) {
                    replyToUsername = replyToUser.getUsername();
                }
            }

            Comment comment = Comment.builder()
                    .discussionId(discussionId)
                    .content(requestDTO.getContent())
                    .userId(userId)
                    .username(userInfo.getUsername())
                    .userAvatar(userInfo.getAvatar())
                    .parentId(requestDTO.getParentId())
                    .rootId(rootId)
                    .path(path)
                    .depth(depth)
                    .replyToUserId(requestDTO.getReplyToUserId())
                    .replyToUsername(replyToUsername)
                    .build();

            Comment savedComment = commentRepository.save(comment);
            
            // 更新讨论的评论数量和最后评论时间
            updateDiscussionCommentInfo(discussionId);

            log.info("创建评论成功: id={}, discussionId={}, userId={}", 
                    savedComment.getId(), discussionId, userId);

            return convertToResponseDTO(savedComment, userId);

        } catch (Exception ex) {
            log.error("创建评论失败: discussionId={}, userId={}, error={}", 
                     discussionId, userId, ex.getMessage());
            throw new RuntimeException("创建评论失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Page<CommentResponseDTO> getCommentsByDiscussionId(
            String discussionId, boolean rootOnly, String sortBy, String order,
            int page, int size, String currentUserId) {

        // 验证讨论是否存在
        discussionRepository.findByIdAndNotDeleted(discussionId)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + discussionId));

        Sort sort = createSort(sortBy, order);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> comments;
        if (rootOnly) {
            // 只获取根评论
            comments = commentRepository.findRootCommentsByDiscussionId(discussionId, pageable);
            
            // 转换为DTO并加载回复
            return comments.map(comment -> {
                CommentResponseDTO dto = convertToResponseDTO(comment, currentUserId);
                List<Comment> replies = commentRepository.findByRootIdAndNotDeleted(comment.getId());
                dto.setReplies(replies.stream()
                        .filter(reply -> !reply.getId().equals(comment.getId())) // 排除自身
                        .map(reply -> convertToResponseDTO(reply, currentUserId))
                        .collect(Collectors.toList()));
                return dto;
            });
        } else {
            // 获取所有评论
            comments = commentRepository.findByDiscussionIdAndNotDeleted(discussionId, pageable);
            return comments.map(comment -> convertToResponseDTO(comment, currentUserId));
        }
    }

    @Override
    public List<CommentResponseDTO> getRepliesByCommentId(String commentId, String currentUserId) {
        List<Comment> replies = commentRepository.findByParentIdAndNotDeleted(commentId);
        return replies.stream()
                .map(reply -> convertToResponseDTO(reply, currentUserId))
                .collect(Collectors.toList());
    }

    @Override
    public Page<CommentResponseDTO> getCommentsByUserId(String userId, int page, int size, String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findByUserIdAndNotDeleted(userId, pageable);
        return comments.map(comment -> convertToResponseDTO(comment, currentUserId));
    }

    @Override
    public CommentResponseDTO getCommentById(String commentId, String currentUserId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));
        return convertToResponseDTO(comment, currentUserId);
    }

    @Override
    @Transactional
    public boolean deleteComment(String commentId, String userId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        // 权限检查：只有作者或管理员可以删除
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此评论");
        }

        // 逻辑删除
        comment.setDeleted(true);
        comment.setDeleteTime(LocalDateTime.now());
        commentRepository.save(comment);

        // 更新讨论的评论数量
        updateDiscussionCommentInfo(comment.getDiscussionId());

        log.info("删除评论成功: id={}, userId={}", commentId, userId);
        return true;
    }

    @Override
    @Transactional
    public CommentResponseDTO toggleLike(String commentId, String userId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        List<String> likedBy = comment.getLikedBy();

        if (likedBy.contains(userId)) {
            // 取消点赞
            likedBy.remove(userId);
            comment.setLikeCount(comment.getLikeCount() - 1);
        } else {
            // 添加点赞
            likedBy.add(userId);
            comment.setLikeCount(comment.getLikeCount() + 1);
        }

        comment.setLikedBy(likedBy);
        comment.setUpdateTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        return convertToResponseDTO(updatedComment, userId);
    }

    @Override
    @Transactional
    public boolean reportComment(String commentId, ReportRequestDTO requestDTO, String reporterId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        comment.setStatus(Comment.CommentStatus.REPORTED);
        comment.setUpdateTime(LocalDateTime.now());
        commentRepository.save(comment);

        log.info("举报评论成功: commentId={}, reporterId={}, reason={}", 
                commentId, reporterId, requestDTO.getReason());
        return true;
    }

    @Override
    @Transactional
    public CommentResponseDTO reviewComment(String commentId, String status, String reviewerId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new RuntimeException("评论不存在: " + commentId));

        Comment.CommentStatus newStatus = Comment.CommentStatus.valueOf(status);
        comment.setStatus(newStatus);
        comment.setUpdateTime(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);
        log.info("审核评论完成: commentId={}, status={}, reviewerId={}", 
                commentId, status, reviewerId);

        return convertToResponseDTO(updatedComment, reviewerId);
    }

    @Override
    public long getUserCommentCount(String userId) {
        return commentRepository.countByUserIdAndNotDeleted(userId);
    }

    @Override
    public long getDiscussionCommentCount(String discussionId) {
        return commentRepository.countByDiscussionIdAndNotDeleted(discussionId);
    }

    @Override
    public Page<CommentResponseDTO> searchComments(String keyword, int page, int size, String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findByContentContaining(keyword, pageable);
        return comments.map(comment -> convertToResponseDTO(comment, currentUserId));
    }

    @Override
    public Page<CommentResponseDTO> getReportedComments(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "updateTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Comment> comments = commentRepository.findReportedComments(pageable);
        return comments.map(comment -> convertToResponseDTO(comment, null));
    }

    @Override
    public List<CommentResponseDTO> getPopularComments(String discussionId, int limit, String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount");
        Pageable pageable = PageRequest.of(0, limit, sort);
        List<Comment> comments = commentRepository.findPopularCommentsByDiscussionId(discussionId, pageable);
        return comments.stream()
                .map(comment -> convertToResponseDTO(comment, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 更新讨论的评论信息
     */
    private void updateDiscussionCommentInfo(String discussionId) {
        try {
            long commentCount = commentRepository.countByDiscussionIdAndNotDeleted(discussionId);
            discussionRepository.findByIdAndNotDeleted(discussionId).ifPresent(discussion -> {
                discussion.setCommentCount(commentCount);
                discussion.setLastCommentTime(LocalDateTime.now());
                discussion.setLastActivityTime(LocalDateTime.now());
                discussionRepository.save(discussion);
            });
        } catch (Exception e) {
            log.warn("更新讨论评论信息失败: discussionId={}, error={}", discussionId, e.getMessage());
        }
    }

    /**
     * 转换为响应DTO
     */
    private CommentResponseDTO convertToResponseDTO(Comment comment, String currentUserId) {
        boolean isLiked = currentUserId != null && comment.getLikedBy().contains(currentUserId);

        return CommentResponseDTO.builder()
                .id(comment.getId())
                .discussionId(comment.getDiscussionId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .username(comment.getUsername())
                .userAvatar(comment.getUserAvatar())
                .parentId(comment.getParentId())
                .rootId(comment.getRootId())
                .path(comment.getPath())
                .depth(comment.getDepth())
                .replyToUserId(comment.getReplyToUserId())
                .replyToUsername(comment.getReplyToUsername())
                .likeCount(comment.getLikeCount())
                .isLiked(isLiked)
                .status(comment.getStatus().name())
                .createTime(comment.getCreateTime())
                .updateTime(comment.getUpdateTime())
                .build();
    }

    /**
     * 创建排序对象
     */
    private Sort createSort(String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "createTime";
        }
        
        return Sort.by(direction, sortBy);
    }
}
