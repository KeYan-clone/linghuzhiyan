package org.linghu.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 发送者信息DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderInfoDTO {

    private String senderId;

    private String senderUsername;

    private String senderRole;

    private String avatar;

    private Long messageCount;

    private Long unreadCount;
}
