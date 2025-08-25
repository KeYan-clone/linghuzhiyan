package org.linghu.discussion.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.discussion.client.UserServiceClient;
import org.linghu.discussion.domain.Discussion;
import org.linghu.discussion.dto.*;
import org.linghu.discussion.repository.DiscussionRepository;
import org.linghu.discussion.service.DiscussionService;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 讨论服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscussionServiceImpl implements DiscussionService {

    private final DiscussionRepository discussionRepository;
    private final UserServiceClient userServiceClient;
    private final MongoTemplate mongoTemplate;

    @Override
    @Transactional
    public DiscussionResponseDTO createDiscussion(DiscussionRequestDTO requestDTO, String userId) {
        try {
            // 获取用户信息
            UserServiceClient.UserInfo userInfo = userServiceClient.getUserById(userId);
            if (userInfo == null) {
                throw new RuntimeException("用户不存在: " + userId);
            }

            Discussion discussion = Discussion.builder()
                    .title(requestDTO.getTitle())
                    .content(requestDTO.getContent())
                    .userId(userId)
                    .username(userInfo.getUsername())
                    .userAvatar(userInfo.getAvatar())
                    .tags(requestDTO.getTags())
                    .experimentId(requestDTO.getExperimentId())
                    .status(Discussion.DiscussionStatus.PENDING)
                    .lastActivityTime(LocalDateTime.now())
                    .build();

            Discussion savedDiscussion = discussionRepository.save(discussion);
            log.info("创建讨论成功: id={}, title={}, userId={}", 
                    savedDiscussion.getId(), savedDiscussion.getTitle(), userId);

            return convertToResponseDTO(savedDiscussion, userId);

        } catch (Exception ex) {
            log.error("创建讨论失败: userId={}, error={}", userId, ex.getMessage());
            throw new RuntimeException("创建讨论失败: " + ex.getMessage(), ex);
        }
    }

    @Override
    public Page<DiscussionResponseDTO> getDiscussions(
            String[] tags, String experimentId, String userId, String status,
            String keyword, String sortBy, String order, int page, int size, String currentUserId) {

        Sort sort = createSort(sortBy, order);
        Pageable pageable = PageRequest.of(page, size, sort);

        // 构建查询条件
        Query query = new Query();
        query.addCriteria(Criteria.where("deleted").is(false));

        // 添加过滤条件
        if (StringUtils.hasText(status)) {
            query.addCriteria(Criteria.where("status").is(status));
        } else {
            // 默认只显示已审核通过的
            query.addCriteria(Criteria.where("status").is("APPROVED"));
        }

        if (tags != null && tags.length > 0) {
            query.addCriteria(Criteria.where("tags").in(Arrays.asList(tags)));
        }

        if (StringUtils.hasText(experimentId)) {
            query.addCriteria(Criteria.where("experimentId").is(experimentId));
        }

        if (StringUtils.hasText(userId)) {
            query.addCriteria(Criteria.where("userId").is(userId));
        }

        if (StringUtils.hasText(keyword)) {
            // 使用正则表达式搜索标题和内容
            Criteria titleCriteria = Criteria.where("title").regex(keyword, "i");
            Criteria contentCriteria = Criteria.where("content").regex(keyword, "i");
            query.addCriteria(new Criteria().orOperator(titleCriteria, contentCriteria));
        }

        // 执行查询
        long total = mongoTemplate.count(query, Discussion.class);
        query.with(pageable);
        List<Discussion> content = mongoTemplate.find(query, Discussion.class);

        Page<Discussion> discussionsPage = new PageImpl<>(content, pageable, total);
        return discussionsPage.map(discussion -> convertToResponseDTO(discussion, currentUserId));
    }

    @Override
    public DiscussionResponseDTO getDiscussionById(String id, String currentUserId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        // 增加浏览次数
        incrementViewCount(id);

        return convertToResponseDTO(discussion, currentUserId);
    }

    @Override
    @Transactional
    public DiscussionResponseDTO updateDiscussion(String id, DiscussionRequestDTO requestDTO, String userId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        // 权限检查：只有作者或管理员可以更新
        if (!discussion.getUserId().equals(userId)) {
            throw new RuntimeException("无权限更新此讨论");
        }

        // 更新内容
        discussion.setTitle(requestDTO.getTitle());
        discussion.setContent(requestDTO.getContent());
        if (requestDTO.getTags() != null) {
            discussion.setTags(requestDTO.getTags());
        }
        if (requestDTO.getExperimentId() != null) {
            discussion.setExperimentId(requestDTO.getExperimentId());
        }

        // 更新后重新设为待审核状态
        discussion.setStatus(Discussion.DiscussionStatus.PENDING);
        discussion.setUpdateTime(LocalDateTime.now());
        discussion.setLastActivityTime(LocalDateTime.now());

        Discussion updatedDiscussion = discussionRepository.save(discussion);
        log.info("更新讨论成功: id={}, userId={}", id, userId);

        return convertToResponseDTO(updatedDiscussion, userId);
    }

    @Override
    @Transactional
    public boolean deleteDiscussion(String id, String userId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        // 权限检查：只有作者或管理员可以删除
        if (!discussion.getUserId().equals(userId)) {
            throw new RuntimeException("无权限删除此讨论");
        }

        // 逻辑删除
        discussion.setDeleted(true);
        discussion.setDeleteTime(LocalDateTime.now());
        discussionRepository.save(discussion);

        log.info("删除讨论成功: id={}, userId={}", id, userId);
        return true;
    }

    @Override
    @Transactional
    public DiscussionResponseDTO reviewDiscussion(String id, ReviewRequestDTO requestDTO, String reviewerId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        Discussion.DiscussionStatus newStatus = Discussion.DiscussionStatus.valueOf(requestDTO.getStatus());
        discussion.setStatus(newStatus);

        if (newStatus == Discussion.DiscussionStatus.REJECTED) {
            discussion.setRejectionReason(requestDTO.getRejectionReason());
        } else if (newStatus == Discussion.DiscussionStatus.APPROVED) {
            discussion.setApprovedTime(LocalDateTime.now());
            discussion.setRejectionReason(null);
        }

        discussion.setUpdateTime(LocalDateTime.now());
        Discussion updatedDiscussion = discussionRepository.save(discussion);

        log.info("审核讨论完成: id={}, status={}, reviewerId={}", id, requestDTO.getStatus(), reviewerId);
        return convertToResponseDTO(updatedDiscussion, reviewerId);
    }

    @Override
    @Transactional
    public DiscussionResponseDTO updatePriority(String id, PriorityRequestDTO requestDTO, String userId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        discussion.setPriority(requestDTO.getPriority());
        discussion.setUpdateTime(LocalDateTime.now());

        Discussion updatedDiscussion = discussionRepository.save(discussion);
        log.info("更新讨论优先级成功: id={}, priority={}, userId={}", id, requestDTO.getPriority(), userId);

        return convertToResponseDTO(updatedDiscussion, userId);
    }

    @Override
    @Transactional
    public DiscussionResponseDTO toggleLike(String id, String userId) {
        Discussion discussion = discussionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("讨论不存在: " + id));

        List<String> likedBy = discussion.getLikedBy();

        if (likedBy.contains(userId)) {
            // 取消点赞
            likedBy.remove(userId);
            discussion.setLikeCount(discussion.getLikeCount() - 1);
        } else {
            // 添加点赞
            likedBy.add(userId);
            discussion.setLikeCount(discussion.getLikeCount() + 1);
        }

        discussion.setLikedBy(likedBy);
        discussion.setUpdateTime(LocalDateTime.now());
        discussion.setLastActivityTime(LocalDateTime.now());

        Discussion updatedDiscussion = discussionRepository.save(discussion);
        return convertToResponseDTO(updatedDiscussion, userId);
    }

    @Override
    @Transactional
    public void incrementViewCount(String id) {
        try {
            discussionRepository.findByIdAndNotDeleted(id).ifPresent(discussion -> {
                discussion.setViewCount(discussion.getViewCount() + 1);
                discussionRepository.save(discussion);
            });
        } catch (Exception e) {
            log.warn("增加浏览次数失败: id={}, error={}", id, e.getMessage());
        }
    }

    @Override
    public long getUserDiscussionCount(String userId) {
        return discussionRepository.countByUserIdAndNotDeleted(userId);
    }

    @Override
    public long getExperimentDiscussionCount(String experimentId) {
        return discussionRepository.countByExperimentIdAndNotDeleted(experimentId);
    }

    @Override
    public Page<DiscussionResponseDTO> getPopularDiscussions(int page, int size, String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount", "viewCount");
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Discussion> discussions = discussionRepository.findPopularDiscussions(pageable);
        return discussions.map(discussion -> convertToResponseDTO(discussion, currentUserId));
    }

    @Override
    public Page<DiscussionResponseDTO> getRecentActiveDiscussions(int page, int size, String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "lastActivityTime");
        Pageable pageable = PageRequest.of(page, size, sort);
        LocalDateTime since = LocalDateTime.now().minusDays(7); // 最近7天
        Page<Discussion> discussions = discussionRepository.findRecentActiveDiscussions(since, pageable);
        return discussions.map(discussion -> convertToResponseDTO(discussion, currentUserId));
    }

    @Override
    public Page<DiscussionResponseDTO> getPinnedDiscussions(String currentUserId) {
        Sort sort = Sort.by(Sort.Direction.DESC, "priority", "lastActivityTime");
        List<Discussion> discussions = discussionRepository.findPinnedDiscussions();
        Page<Discussion> discussionPage = new PageImpl<>(discussions);
        return discussionPage.map(discussion -> convertToResponseDTO(discussion, currentUserId));
    }

    /**
     * 转换为响应DTO
     */
    private DiscussionResponseDTO convertToResponseDTO(Discussion discussion, String currentUserId) {
        boolean isLiked = currentUserId != null && discussion.getLikedBy().contains(currentUserId);

        return DiscussionResponseDTO.builder()
                .id(discussion.getId())
                .title(discussion.getTitle())
                .content(discussion.getContent())
                .userId(discussion.getUserId())
                .username(discussion.getUsername())
                .userAvatar(discussion.getUserAvatar())
                .tags(discussion.getTags())
                .experimentId(discussion.getExperimentId())
                .status(discussion.getStatus().name())
                .rejectionReason(discussion.getRejectionReason())
                .priority(discussion.getPriority())
                .viewCount(discussion.getViewCount())
                .commentCount(discussion.getCommentCount())
                .likeCount(discussion.getLikeCount())
                .isLiked(isLiked)
                .lastCommentTime(discussion.getLastCommentTime())
                .lastActivityTime(discussion.getLastActivityTime())
                .createTime(discussion.getCreateTime())
                .updateTime(discussion.getUpdateTime())
                .approvedTime(discussion.getApprovedTime())
                .build();
    }

    /**
     * 创建排序对象
     */
    private Sort createSort(String sortBy, String order) {
        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        
        // 默认排序字段
        if (!StringUtils.hasText(sortBy)) {
            sortBy = "lastActivityTime";
        }
        
        return Sort.by(direction, sortBy);
    }

}
