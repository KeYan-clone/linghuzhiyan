package org.linghu.message.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/api/v1/users/internal/{id}")
    UserInfo getUserById(@PathVariable("id") String id);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/api/v1/users/internal/username/{username}")
    UserInfo getUserByUsername(@PathVariable("username") String username);

    /**
     * 用户信息类
     */
    class UserInfo {
        private String id;
        private String username;
        private String email;
        private String avatar;
        private String role;

        // Getters and setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getAvatar() {
            return avatar;
        }

        public void setAvatar(String avatar) {
            this.avatar = avatar;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
