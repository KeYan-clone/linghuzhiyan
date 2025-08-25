package org.linghu.discussion.repository;

import org.linghu.discussion.domain.Discussion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 讨论数据访问接口
 */
@Repository
public interface DiscussionRepository extends MongoRepository<Discussion, String> {

    /**
     * 查找未删除的讨论
     */
    @Query("{'deleted': false, '_id': ?0}")
    Optional<Discussion> findByIdAndNotDeleted(String id);

    /**
     * 查找未删除的讨论列表
     */
    @Query("{'deleted': false}")
    Page<Discussion> findAllByNotDeleted(Pageable pageable);

    /**
     * 按用户ID查找讨论
     */
    @Query("{'deleted': false, 'userId': ?0}")
    Page<Discussion> findByUserIdAndNotDeleted(String userId, Pageable pageable);

    /**
     * 按实验ID查找讨论
     */
    @Query("{'deleted': false, 'experimentId': ?0}")
    Page<Discussion> findByExperimentIdAndNotDeleted(String experimentId, Pageable pageable);

    /**
     * 按状态查找讨论
     */
    @Query("{'deleted': false, 'status': ?0}")
    Page<Discussion> findByStatusAndNotDeleted(String status, Pageable pageable);

    /**
     * 按标签查找讨论
     */
    @Query("{'deleted': false, 'tags': {'$in': ?0}}")
    Page<Discussion> findByTagsInAndNotDeleted(List<String> tags, Pageable pageable);

    /**
     * 全文搜索讨论
     */
    @Query("{'deleted': false, '$text': {'$search': ?0}}")
    Page<Discussion> findByTextSearch(String keyword, Pageable pageable);

    /**
     * 按标题关键词搜索
     */
    @Query("{'deleted': false, 'title': {'$regex': ?0, '$options': 'i'}}")
    Page<Discussion> findByTitleContaining(String keyword, Pageable pageable);

    /**
     * 按内容关键词搜索
     */
    @Query("{'deleted': false, 'content': {'$regex': ?0, '$options': 'i'}}")
    Page<Discussion> findByContentContaining(String keyword, Pageable pageable);

    /**
     * 查找用户的讨论数量
     */
    @Query(value = "{'deleted': false, 'userId': ?0}", count = true)
    long countByUserIdAndNotDeleted(String userId);

    /**
     * 查找实验的讨论数量
     */
    @Query(value = "{'deleted': false, 'experimentId': ?0}", count = true)
    long countByExperimentIdAndNotDeleted(String experimentId);

    /**
     * 查找热门讨论（按点赞数）
     */
    @Query("{'deleted': false, 'status': 'APPROVED'}")
    Page<Discussion> findPopularDiscussions(Pageable pageable);

    /**
     * 查找最新活动讨论
     */
    @Query("{'deleted': false, 'status': 'APPROVED', 'lastActivityTime': {'$gte': ?0}}")
    Page<Discussion> findRecentActiveDiscussions(LocalDateTime since, Pageable pageable);

    /**
     * 查找置顶讨论
     */
    @Query("{'deleted': false, 'status': 'APPROVED', 'priority': {'$gt': 0}}")
    List<Discussion> findPinnedDiscussions();

    /**
     * 更新讨论的评论数量
     */
    @Query("{'_id': ?0}")
    void updateCommentCount(String discussionId, long commentCount);

    /**
     * 增加浏览次数
     */
    @Query("{'_id': ?0}")
    void incrementViewCount(String discussionId);

    /**
     * 批量更新最后活动时间
     */
    @Query("{'_id': {'$in': ?0}}")
    void updateLastActivityTime(List<String> discussionIds, LocalDateTime lastActivityTime);
}
