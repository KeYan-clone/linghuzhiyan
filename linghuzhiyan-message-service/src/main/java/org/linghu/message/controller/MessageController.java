package org.linghu.message.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.linghu.message.dto.MessageDTO;
import org.linghu.message.dto.MessageQueryDTO;
import org.linghu.message.dto.MessageRequestDTO;
import org.linghu.message.dto.SenderInfoDTO;
import org.linghu.message.service.MessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Validated
public class MessageController {

    private final MessageService messageService;

    /**
     * 创建消息
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> createMessage(@Valid @RequestBody MessageRequestDTO requestDTO) {
        try {
            String currentUser = getCurrentUserId();
            MessageDTO messageDTO = messageService.createMessage(requestDTO, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息创建成功");
            response.put("data", messageDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("创建消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取单个消息详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getMessageById(@PathVariable @NotNull String id) {
        try {
            MessageDTO messageDTO = messageService.getMessageById(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messageDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取消息详情失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取消息详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户接收的消息
     */
    @GetMapping("/received")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getReceivedMessages() {
        try {
            String currentUser = getCurrentUserId();
            List<MessageDTO> messages = messageService.getMessagesByReceiver(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取接收消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取接收消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户发送的消息
     */
    @GetMapping("/sent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getSentMessages() {
        try {
            String currentUser = getCurrentUserId();
            List<MessageDTO> messages = messageService.getMessagesBySender(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取发送消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取发送消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取与特定用户的对话
     */
    @GetMapping("/conversation/{otherUserId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getConversation(@PathVariable @NotNull String otherUserId) {
        try {
            String currentUser = getCurrentUserId();
            List<MessageDTO> messages = messageService.getMessagesBySenderAndReceiver(currentUser, otherUserId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取对话失败: otherUserId={}", otherUserId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取对话失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取所有消息（仅管理员）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAllMessages() {
        try {
            List<MessageDTO> messages = messageService.getAllMessages();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取所有消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取所有消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable @NotNull String id) {
        try {
            MessageDTO messageDTO = messageService.markAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息已标记为已读");
            response.put("data", messageDTO);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("标记消息为已读失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "标记消息为已读失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量标记消息为已读
     */
    @PutMapping("/batch-read")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> batchMarkAsRead(@RequestBody @NotEmpty List<String> ids) {
        try {
            String currentUser = getCurrentUserId();
            messageService.batchMarkAsRead(ids, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "批量标记消息为已读成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("批量标记消息为已读失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量标记消息为已读失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除消息
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> deleteMessage(@PathVariable @NotNull String id) {
        try {
            String currentUser = getCurrentUserId();
            messageService.deleteMessage(id, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "消息删除成功");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("删除消息失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取发送者信息
     */
    @GetMapping("/senders")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getSenders() {
        try {
            String currentUser = getCurrentUserId();
            List<SenderInfoDTO> senders = messageService.getSendersByReceiver(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", senders);
            response.put("total", senders.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取发送者信息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取发送者信息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 分页查询消息
     */
    @PostMapping("/query")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> queryMessages(@Valid @RequestBody MessageQueryDTO queryDTO) {
        try {
            Page<MessageDTO> page = messageService.queryMessages(queryDTO);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", page.getContent());
            response.put("total", page.getTotalElements());
            response.put("page", page.getNumber());
            response.put("size", page.getSize());
            response.put("totalPages", page.getTotalPages());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("查询消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "查询消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 搜索消息
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> searchMessages(@RequestParam @NotEmpty String keyword) {
        try {
            String currentUser = getCurrentUserId();
            List<MessageDTO> messages = messageService.searchMessages(keyword, currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            response.put("keyword", keyword);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("搜索消息失败: keyword={}", keyword, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "搜索消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getUnreadCount() {
        try {
            String currentUser = getCurrentUserId();
            long unreadCount = messageService.getUnreadCount(currentUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", unreadCount);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读消息数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取最近消息
     */
    @GetMapping("/recent")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Map<String, Object>> getRecentMessages(@RequestParam(defaultValue = "10") int limit) {
        try {
            String currentUser = getCurrentUserId();
            List<MessageDTO> messages = messageService.getRecentMessages(currentUser, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", messages);
            response.put("total", messages.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("获取最近消息失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取最近消息失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        throw new RuntimeException("用户未认证");
    }
}
