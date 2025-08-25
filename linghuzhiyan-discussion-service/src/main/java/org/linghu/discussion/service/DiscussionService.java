package org.linghu.discussion.service;

import org.linghu.discussion.dto.*;
import org.springframework.data.domain.Page;

/**
 * 讨论服务接口
 */
public interface DiscussionService {

    /**
     * 创建讨论
     */
    DiscussionResponseDTO createDiscussion(DiscussionRequestDTO requestDTO, String userId);

    /**
     * 获取讨论列表
     */
    Page<DiscussionResponseDTO> getDiscussions(
            String[] tags,
            String experimentId,
            String userId,
            String status,
            String keyword,
            String sortBy,
            String order,
            int page,
            int size,
            String currentUserId);

    /**
     * 获取讨论详情
     */
    DiscussionResponseDTO getDiscussionById(String id, String currentUserId);

    /**
     * 更新讨论
     */
    DiscussionResponseDTO updateDiscussion(String id, DiscussionRequestDTO requestDTO, String userId);

    /**
     * 删除讨论
     */
    boolean deleteDiscussion(String id, String userId);

    /**
     * 审核讨论
     */
    DiscussionResponseDTO reviewDiscussion(String id, ReviewRequestDTO requestDTO, String reviewerId);

    /**
     * 更新讨论优先级
     */
    DiscussionResponseDTO updatePriority(String id, PriorityRequestDTO requestDTO, String userId);

    /**
     * 点赞/取消点赞讨论
     */
    DiscussionResponseDTO toggleLike(String id, String userId);

    /**
     * 增加浏览次数
     */
    void incrementViewCount(String id);

    /**
     * 获取用户的讨论统计
     */
    long getUserDiscussionCount(String userId);

    /**
     * 获取实验的讨论统计
     */
    long getExperimentDiscussionCount(String experimentId);

    /**
     * 获取热门讨论
     */
    Page<DiscussionResponseDTO> getPopularDiscussions(int page, int size, String currentUserId);

    /**
     * 获取最新活动讨论
     */
    Page<DiscussionResponseDTO> getRecentActiveDiscussions(int page, int size, String currentUserId);

    /**
     * 获取置顶讨论
     */
    Page<DiscussionResponseDTO> getPinnedDiscussions(String currentUserId);
}
