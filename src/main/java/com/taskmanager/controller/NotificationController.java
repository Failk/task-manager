package com.taskmanager.controller;

import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.dto.notification.NotificationDTO;
import com.taskmanager.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Notification management using Observer Pattern")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notification history")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getAllNotifications() {
        List<NotificationDTO> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @Operation(summary = "Get unread notifications")
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>> getUnreadNotifications() {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications();
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(Map.of("unreadCount", count)));
    }

    @Operation(summary = "Acknowledge notification")
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<NotificationDTO>> acknowledgeNotification(@PathVariable Long id) {
        NotificationDTO notification = notificationService.acknowledgeNotification(id);
        return ResponseEntity.ok(ApiResponse.success("Notification acknowledged", notification));
    }

    @Operation(summary = "Snooze notification/reminder")
    @PostMapping("/{id}/snooze")
    public ResponseEntity<ApiResponse<Void>> snoozeNotification(
            @PathVariable Long id,
            @RequestParam(defaultValue = "15") int minutes) {
        notificationService.snoozeReminder(id, minutes);
        return ResponseEntity.ok(ApiResponse.success("Notification snoozed for " + minutes + " minutes", null));
    }
}
