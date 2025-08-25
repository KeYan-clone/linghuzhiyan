package org.linghu.message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.message.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统通知控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
public class NotificationController {

    private final MessageService messageService;

    /**
     * 发送系统通知请求DTO
     */
    public static class SystemNotificationRequest {
        @NotEmpty(message = "标题不能为空")
        private String title;
        
        @NotEmpty(message = "内容不能为空")
        private String content;
        
        @NotEmpty(message = "接收者列表不能为空")
        private List<String> receiverIds;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public List<String> getReceiverIds() {
            return receiverIds;
        }

        public void setReceiverIds(List<String> receiverIds) {
            this.receiverIds = receiverIds;
        }
    }

    /**
     * 发送实验通知请求DTO
     */
    public static class ExperimentNotificationRequest {
        @NotEmpty(message = "标题不能为空")
        private String title;
        
        @NotEmpty(message = "内容不能为空")
        private String content;
        
        @NotNull(message = "实验ID不能为空")
        private String experimentId;
        
        @NotEmpty(message = "接收者列表不能为空")
        private List<String> receiverIds;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getExperimentId() {
            return experimentId;
        }

        public void setExperimentId(String experimentId) {
            this.experimentId = experimentId;
        }

        public List<String> getReceiverIds() {
            return receiverIds;
        }

        public void setReceiverIds(List<String> receiverIds) {
            this.receiverIds = receiverIds;
        }
    }

    /**
     * 发送成绩通知请求DTO
     */
    public static class GradeNotificationRequest {
        @NotEmpty(message = "标题不能为空")
        private String title;
        
        @NotEmpty(message = "内容不能为空")
        private String content;
        
        @NotNull(message = "实验ID不能为空")
        private String experimentId;
        
        @NotNull(message = "接收者ID不能为空")
        private String receiverId;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getExperimentId() {
            return experimentId;
        }

        public void setExperimentId(String experimentId) {
            this.experimentId = experimentId;
        }

        public String getReceiverId() {
            return receiverId;
        }

        public void setReceiverId(String receiverId) {
            this.receiverId = receiverId;
        }
    }

    /**
     * 发送系统通知
     */
    @PostMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendSystemNotification(
            @Valid @RequestBody SystemNotificationRequest request) {
        try {
            messageService.sendSystemNotification(
                    request.getTitle(),
                    request.getContent(),
                    request.getReceiverIds()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "系统通知发送成功");
            response.put("receiverCount", request.getReceiverIds().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发送系统通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送实验通知
     */
    @PostMapping("/experiment")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> sendExperimentNotification(
            @Valid @RequestBody ExperimentNotificationRequest request) {
        try {
            messageService.sendExperimentNotification(
                    request.getTitle(),
                    request.getContent(),
                    request.getExperimentId(),
                    request.getReceiverIds()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "实验通知发送成功");
            response.put("experimentId", request.getExperimentId());
            response.put("receiverCount", request.getReceiverIds().size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送实验通知失败: experimentId={}", request.getExperimentId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发送实验通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送成绩通知
     */
    @PostMapping("/grade")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Map<String, Object>> sendGradeNotification(
            @Valid @RequestBody GradeNotificationRequest request) {
        try {
            messageService.sendGradeNotification(
                    request.getTitle(),
                    request.getContent(),
                    request.getExperimentId(),
                    request.getReceiverId()
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "成绩通知发送成功");
            response.put("experimentId", request.getExperimentId());
            response.put("receiverId", request.getReceiverId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("发送成绩通知失败: experimentId={}, receiverId={}", 
                     request.getExperimentId(), request.getReceiverId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发送成绩通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
