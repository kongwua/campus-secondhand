package com.campus.secondhand.controller;

import com.campus.secondhand.dto.request.MessageSendRequest;
import com.campus.secondhand.dto.response.ApiResponse;
import com.campus.secondhand.dto.response.MessageResponse;
import com.campus.secondhand.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getMessageList() {
        Long userId = UserContext.getCurrentUserId();
        List<Map<String, Object>> messages = messageService.getMessageList(userId);
        return ApiResponse.success(messages);
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> send(@Valid @RequestBody MessageSendRequest request) {
        Long userId = UserContext.getCurrentUserId();
        Long messageId = messageService.send(userId, request);

        Map<String, Object> result = new HashMap<>();
        result.put("id", messageId);
        return ApiResponse.success("消息发送成功", result);
    }

    @GetMapping("/conversation/{userId}")
    public ApiResponse<List<MessageResponse>> getConversation(@PathVariable Long userId) {
        Long currentUserId = UserContext.getCurrentUserId();
        List<MessageResponse> messages = messageService.getConversation(currentUserId, userId);
        return ApiResponse.success(messages);
    }

    @PutMapping("/{id}/read")
    public ApiResponse<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        messageService.markAsRead(id, userId);
        return ApiResponse.success("消息已标记为已读");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> getUnreadCount() {
        Long userId = UserContext.getCurrentUserId();
        int count = messageService.getUnreadCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return ApiResponse.success(result);
    }
}