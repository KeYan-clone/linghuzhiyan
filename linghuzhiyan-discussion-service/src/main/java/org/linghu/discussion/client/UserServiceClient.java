package org.linghu.discussion.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{id}")
    UserInfo getUserById(@PathVariable("id") String id);

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    class UserInfo {
        private String id;
        private String username;
        private String email;
        private String role;
        private String avatar;
        private String department;
        private Boolean active;
    }
}
