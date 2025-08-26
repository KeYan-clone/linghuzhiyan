package org.linghu.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.Set;

/**
 * 用户基本信息DTO（用于认证服务）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    
    private String id;
    private String username;
    private String email;
    private Boolean isDeleted;
    private Set<String> roles;
    private Date createdAt;
}
