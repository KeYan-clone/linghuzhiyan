package org.linghu.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionResponseDTO {

    private String id;
    private String title;
    private String content;
    private String userId;
    private String username;
    private String userAvatar;
    private List<String> tags;
    private String experimentId;
    private String status;
    private String rejectionReason;
    private Integer priority;
    private Long viewCount;
    private Long commentCount;
    private Long likeCount;
    private Boolean isLiked;
    private LocalDateTime lastCommentTime;
    private LocalDateTime lastActivityTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime approvedTime;
}
