package org.linghu.experiment.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.experiment.client.UserServiceClient;
import org.linghu.experiment.domain.ExperimentAssignment;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.repository.ExperimentAssignmentRepository;
import org.linghu.experiment.repository.ExperimentRepository;
import org.linghu.experiment.repository.ExperimentTaskRepository;
import org.linghu.experiment.service.ExperimentAssignmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 实验任务分配管理服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExperimentAssignmentServiceImpl implements ExperimentAssignmentService {

    private final ExperimentAssignmentRepository assignmentRepository;
    private final ExperimentRepository experimentRepository;
    private final UserServiceClient userServiceClient;
    private final ExperimentTaskRepository experimentTaskRepository;

    @Override
    @Transactional
    public void assignTask(String taskId, String userId) {
        log.info("分配任务: taskId={}, userId={}", taskId, userId);
        
        // 验证实验任务和用户是否存在
        experimentTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("实验任务不存在"));

        UserDTO user = userServiceClient.getUserByIdInExp(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 检查是否已分配
        if (assignmentRepository.existsByTaskIdAndUserId(taskId, userId)) {
            throw new RuntimeException("该实验任务已分配给此用户");
        }

        // 检查用户是否为学生（使用辅助方法）
        if (!isStudentUser(user)) {
            throw new RuntimeException("只能将实验任务分配给学生角色的用户");
        }

        // 创建分配记录
        ExperimentAssignment assignment = new ExperimentAssignment();
        assignment.setId(UUID.randomUUID().toString());
        assignment.setTaskId(taskId);
        assignment.setUserId(userId);

        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public void batchAssignTask(String taskId, List<String> userIds) {
        log.info("批量分配任务: taskId={}, userIds={}", taskId, userIds);
        
        // 验证实验任务是否存在
        experimentTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("实验任务不存在"));

        // 批量获取用户信息
        List<UserDTO> users = userServiceClient.getUsersByIdsInExp(userIds);
        
        // 逐个分配
        for (String userId : userIds) {
            try {
                if (!assignmentRepository.existsByTaskIdAndUserId(taskId, userId)) {
                    UserDTO user = users.stream()
                            .filter(u -> u.getId().equals(userId))
                            .findFirst()
                            .orElse(null);

                    if (user != null && isStudentUser(user)) {
                        ExperimentAssignment assignment = new ExperimentAssignment();
                        assignment.setId(UUID.randomUUID().toString());
                        assignment.setTaskId(taskId);
                        assignment.setUserId(userId);

                        assignmentRepository.save(assignment);
                    }
                }
            } catch (Exception e) {
                // 记录错误但继续处理其他用户
                System.err.println("分配实验任务失败，用户ID: " + userId + ", 错误: " + e.getMessage());
            }
        }
    }

    /**
     * 判断用户是否为学生角色
     *
     * @param user 用户对象
     * @return 是否为学生
     */
    private boolean isStudentUser(UserDTO user) {
        return user.getRoles().contains("STUDENT");
    }

    @Override
    public List<UserDTO> getTaskAssignments(String taskId) {
        // 验证实验任务是否存在
        experimentTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("实验任务不存在"));

        // 获取分配给该实验任务的所有用户ID
        List<String> userIds = assignmentRepository.findByTaskId(taskId)
                .stream()
                .map(ExperimentAssignment::getUserId)
                .collect(Collectors.toList());

        // 获取用户详情
        List<UserDTO> users = userServiceClient.getUsersByIdsInExp(userIds);

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void assignTaskToAllStudents(String taskId) {
        // 获取所有学生用户
        List<UserDTO> students = getAllStudentUsers();

        // 批量分配
        List<String> studentIds = students.stream()
                .map(UserDTO::getId)
                .collect(Collectors.toList());

        batchAssignTask(taskId, studentIds);
    }

    /**
     * 获取所有学生用户
     *
     * @return 学生用户列表
     */
    private List<UserDTO> getAllStudentUsers() {
        // 获取所有用户并过滤出学生
        List<UserDTO> allUsers = userServiceClient.getAllUsersInExp();
        return allUsers.stream()
                .filter(this::isStudentUser)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeTaskAssignment(String taskId, String userId) {
        // 验证分配是否存在
        ExperimentAssignment assignment = assignmentRepository
                .findByTaskIdAndUserId(taskId, userId)
                .orElseThrow(() -> new RuntimeException("未找到该实验任务分配"));

        // 删除分配记录
        assignmentRepository.delete(assignment);
    }

    /**
     * 将用户实体转换为DTO
     * 
     * @param user 用户实体
     * @return 用户DTO
     */
    private UserDTO convertToDTO(UserDTO user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        return dto;
    }
}
