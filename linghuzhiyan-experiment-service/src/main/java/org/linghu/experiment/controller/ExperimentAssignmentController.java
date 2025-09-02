package org.linghu.experiment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.linghu.experiment.dto.Result;
import org.linghu.experiment.dto.UserDTO;
import org.linghu.experiment.service.ExperimentAssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 实验分配API控制器
 */
@RestController
@RequestMapping("/api/experiments/assignments")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
@Tag(name = "实验分配管理", description = "实验分配给学生相关API")
public class ExperimentAssignmentController {

    private final ExperimentAssignmentService experimentAssignmentService;

    @Autowired
    public ExperimentAssignmentController(ExperimentAssignmentService experimentAssignmentService) {
        this.experimentAssignmentService = experimentAssignmentService;
    }

    @PostMapping("/{taskId}")
    @Operation(summary = "分配实验任务给学生", description = "将实验任务分配给一个或多个学生")
    public Result<Void> assignTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> assignment) {

        if (assignment.containsKey("userId")) {
            // 向后兼容：单个用户分配
            String userId = (String) assignment.get("userId");
            experimentAssignmentService.assignTask(taskId, userId);
        } else if (assignment.containsKey("userIds")) {
            // 批量分配给多个用户
            @SuppressWarnings("unchecked")
            List<String> userIds = (List<String>) assignment.get("userIds");
            experimentAssignmentService.batchAssignTask(taskId, userIds);
        } else {
            return Result.error(400, "请提供userId或userIds参数");
        }
        return Result.success();
    }

    @PostMapping("/{taskId}/all")
    @Operation(summary = "分配实验任务给全部学生", description = "将实验任务分配给所有学生")
    public Result<Void> assignTaskToAllStudents(@PathVariable String taskId) {
        experimentAssignmentService.assignTaskToAllStudents(taskId);
        return Result.success();
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "获取实验任务分配列表", description = "获取实验任务分配给的学生列表")
    public Result<List<UserDTO>> getTaskAssignments(@PathVariable String taskId) {
        List<UserDTO> users = experimentAssignmentService.getTaskAssignments(taskId);
        return Result.success(users);
    }

    @DeleteMapping("/{taskId}/{userId}")
    @Operation(summary = "取消实验任务分配", description = "取消将实验任务分配给特定学生")
    public Result<Void> removeTaskAssignment(
            @PathVariable String taskId,
            @PathVariable String userId) {

        experimentAssignmentService.removeTaskAssignment(taskId, userId);
        return Result.success();
    }
}
