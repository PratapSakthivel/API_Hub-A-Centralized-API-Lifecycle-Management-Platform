package com.apihub.backend.features.notifications.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.notifications.dto.CreateNotificationRequest;
import com.apihub.backend.features.notifications.dto.NotificationDto;
import com.apihub.backend.features.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDto>>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUserNotifications(userId, unreadOnly)));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(@RequestParam(required = false) Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<NotificationDto>> createNotification(@RequestBody CreateNotificationRequest request) {
        NotificationDto created = notificationService.createNotification(request);
        return ResponseEntity.status(201).body(ApiResponse.success(created));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationDto>> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.markAsRead(id)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(@RequestParam(required = false) Long userId) {
        int updated = notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("updatedCount", updated)));
    }
}
