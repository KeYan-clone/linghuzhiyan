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
 * 评论实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comments")
public class Comment {

    @Id
    private String id;

    /**
     * 讨论ID
     */
    @Field("discussion_id")
    private String discussionId;

    /**
     * 评论内容（纯文本）
     */
    @Field("content")
    private String content;

    /**
     * 评论用户ID
     */
    @Field("user_id")
    private String userId;

    /**
     * 评论用户名
     */
    @Field("username")
    private String username;

    /**
     * 用户头像
     */
    @Field("user_avatar")
    private String userAvatar;

    /**
     * 父评论ID（直接回复的评论ID）
     */
    @Field("parent_id")
    private String parentId;

    /**
     * 根评论ID（评论树的根节点）
     */
    @Field("root_id")
    private String rootId;

    /**
     * 评论路径（用于评论树排序）
     */
    @Field("path")
    private String path;

    /**
     * 评论深度
     */
    @Field("depth")
    @Builder.Default
    private Integer depth = 0;

    /**
     * 回复目标用户ID
     */
    @Field("reply_to_user_id")
    private String replyToUserId;

    /**
     * 回复目标用户名
     */
    @Field("reply_to_username")
    private String replyToUsername;

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
     * 评论状态：NORMAL-正常，REPORTED-被举报，HIDDEN-已隐藏
     */
    @Field("status")
    @Builder.Default
    private CommentStatus status = CommentStatus.NORMAL;

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
     * 评论状态枚举
     */
    public enum CommentStatus {
        NORMAL("NORMAL", "正常"),
        REPORTED("REPORTED", "被举报"),
        HIDDEN("HIDDEN", "已隐藏");

        private final String code;
        private final String description;

        CommentStatus(String code, String description) {
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
