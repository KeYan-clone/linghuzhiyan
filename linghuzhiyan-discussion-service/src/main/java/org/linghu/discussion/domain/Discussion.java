package org.linghu.discussion.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 讨论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "discussions")
public class Discussion {

    @Id
    private String id;

    /**
     * 讨论标题
     */
    @Field("title")
    private String title;

    /**
     * 讨论内容（纯文本）
     */
    @Field("content")
    private String content;

    /**
     * 创建者用户ID
     */
    @Field("user_id")
    private String userId;

    /**
     * 创建者用户名
     */
    @Field("username")
    private String username;

    /**
     * 创建者头像
     */
    @Field("user_avatar")
    private String userAvatar;

    /**
     * 标签列表
     */
    @Field("tags")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * 关联实验ID
     */
    @Field("experiment_id")
    private String experimentId;

    /**
     * 审核状态：PENDING-待审核，APPROVED-已通过，REJECTED-已拒绝
     */
    @Field("status")
    @Builder.Default
    private DiscussionStatus status = DiscussionStatus.PENDING;

    /**
     * 拒绝原因
     */
    @Field("rejection_reason")
    private String rejectionReason;

    /**
     * 优先级（用于置顶等）
     */
    @Field("priority")
    @Builder.Default
    private Integer priority = 0;

    /**
     * 浏览次数
     */
    @Field("view_count")
    @Builder.Default
    private Long viewCount = 0L;

    /**
     * 评论数量
     */
    @Field("comment_count")
    @Builder.Default
    private Long commentCount = 0L;

    /**
     * 点赞数量
     */
    @Field("like_count")
    @Builder.Default
    private Long likeCount = 0L;

    /**
     * 点赞用户列表
     */
    @Field("liked_by")
    @Builder.Default
    private List<String> likedBy = new ArrayList<>();

    /**
     * 最后评论时间
     */
    @Field("last_comment_time")
    private LocalDateTime lastCommentTime;

    /**
     * 最后活动时间
     */
    @Field("last_activity_time")
    private LocalDateTime lastActivityTime;

    /**
     * 创建时间
     */
    @Field("create_time")
    @Builder.Default
    private LocalDateTime createTime = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Field("update_time")
    @Builder.Default
    private LocalDateTime updateTime = LocalDateTime.now();

    /**
     * 审核通过时间
     */
    @Field("approved_time")
    private LocalDateTime approvedTime;

    /**
     * 上一次审核通过版本的快照（当编辑后回到未通过状态时，仍可对外展示该快照）
     */
    @Field("last_approved_title")
    private String lastApprovedTitle;

    @Field("last_approved_content")
    private String lastApprovedContent;

    @Field("last_approved_tags")
    @Builder.Default
    private List<String> lastApprovedTags = new ArrayList<>();

    @Field("last_approved_time")
    private LocalDateTime lastApprovedTime;

    /**
     * 是否删除
     */
    @Field("deleted")
    @Builder.Default
    private Boolean deleted = false;

    /**
     * 删除时间
     */
    @Field("delete_time")
    private LocalDateTime deleteTime;

    /**
     * 讨论状态枚举
     */
    public enum DiscussionStatus {
        PENDING("PENDING", "待审核"),
        APPROVED("APPROVED", "已通过"),
        REJECTED("REJECTED", "已拒绝");

        private final String code;
        private final String description;

        DiscussionStatus(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
