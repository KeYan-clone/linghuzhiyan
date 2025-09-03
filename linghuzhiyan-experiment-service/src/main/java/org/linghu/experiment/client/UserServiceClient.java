package org.linghu.experiment.client;

import org.linghu.experiment.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 用户服务Feign客户端
 */
@FeignClient(name = "linghuzhiyan-user-service",path = "/api/internal/users")
public interface UserServiceClient {

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/exp/{id}")
    UserDTO getUserByIdInExp(@PathVariable("id") String id);

    /**
     * 根据用户名获取用户信息
     */
    @GetMapping("/exp/username/{username}")
    UserDTO getUserByUsernameInExp(@PathVariable("username") String username);

    /**
     * 批量获取用户信息
     */
    @PostMapping("/exp/batch")
    List<UserDTO> getUsersByIdsInExp(@RequestBody List<String> userIds);

    /**
     * 获取所有用户信息
     */
    @GetMapping("/exp/all")
    List<UserDTO> getAllUsersInExp();
}
