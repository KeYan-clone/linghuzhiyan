package org.linghu.discussion.repository;

import org.linghu.discussion.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 评论数据访问接口
 */
@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {

    /**
     * 查找未删除的评论
     */
    @Query("{'deleted': false, '_id': ?0}")
    Optional<Comment> findByIdAndNotDeleted(String id);

    /**
     * 查找讨论的所有评论
     */
    @Query("{'deleted': false, 'discussionId': ?0}")
    Page<Comment> findByDiscussionIdAndNotDeleted(String discussionId, Pageable pageable);

    /**
     * 查找讨论的根评论（顶级评论）
     */
    @Query("{'deleted': false, 'discussionId': ?0, 'parentId': null}")
    Page<Comment> findRootCommentsByDiscussionId(String discussionId, Pageable pageable);

    /**
     * 查找评论的回复
     */
    @Query("{'deleted': false, 'parentId': ?0}")
    List<Comment> findByParentIdAndNotDeleted(String parentId);

    /**
     * 查找评论的所有子评论（根据根评论ID）
     */
    @Query("{'deleted': false, 'rootId': ?0}")
    List<Comment> findByRootIdAndNotDeleted(String rootId);

    /**
     * 查找用户的评论
     */
    @Query("{'deleted': false, 'userId': ?0}")
    Page<Comment> findByUserIdAndNotDeleted(String userId, Pageable pageable);

    /**
     * 查找用户在特定讨论中的评论
     */
    @Query("{'deleted': false, 'discussionId': ?0, 'userId': ?1}")
    List<Comment> findByDiscussionIdAndUserIdAndNotDeleted(String discussionId, String userId);

    /**
     * 统计讨论的评论数量
     */
    @Query(value = "{'deleted': false, 'discussionId': ?0}", count = true)
    long countByDiscussionIdAndNotDeleted(String discussionId);

    /**
     * 统计用户的评论数量
     */
    @Query(value = "{'deleted': false, 'userId': ?0}", count = true)
    long countByUserIdAndNotDeleted(String userId);

    /**
     * 查找最近的评论
     */
    @Query("{'deleted': false, 'discussionId': ?0}")
    List<Comment> findRecentCommentsByDiscussionId(String discussionId, Pageable pageable);

    /**
     * 查找热门评论（按点赞数）
     */
    @Query("{'deleted': false, 'discussionId': ?0}")
    List<Comment> findPopularCommentsByDiscussionId(String discussionId, Pageable pageable);

    /**
     * 按内容搜索评论
     */
    @Query("{'deleted': false, 'content': {'$regex': ?0, '$options': 'i'}}")
    Page<Comment> findByContentContaining(String keyword, Pageable pageable);

    /**
     * 查找需要审核的评论
     */
    @Query("{'deleted': false, 'status': 'REPORTED'}")
    Page<Comment> findReportedComments(Pageable pageable);

    /**
     * 删除讨论的所有评论（逻辑删除）
     */
    @Query("{'discussionId': ?0}")
    void markDeletedByDiscussionId(String discussionId);

    /**
     * 查找评论树的路径
     */
    @Query("{'deleted': false, 'path': {'$regex': '^?0'}}")
    List<Comment> findByPathStartingWith(String pathPrefix);
}
