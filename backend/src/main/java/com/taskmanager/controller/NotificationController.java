package com.taskmanager.controller;

import com.taskmanager.entity.NotificationPreferences;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.User;
import com.taskmanager.repository.ReminderRepository;
import com.taskmanager.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final ReminderRepository reminderRepository;
    
    @GetMapping("/preferences")
    @Operation(summary = "Get notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> getPreferences(
            @AuthenticationPrincipal User user) {
        NotificationPreferences prefs = notificationService.getPreferences(user.getId());
        return ResponseEntity.ok(toResponse(prefs));
    }
    
    @PutMapping("/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<NotificationPreferencesResponse> updatePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdatePreferencesRequest request) {
        NotificationPreferences prefs = notificationService.updatePreferences(user.getId(), request);
        return ResponseEntity.ok(toResponse(prefs));
    }
    
    @GetMapping("/pending")
    @Operation(summary = "Get pending reminders")
    public ResponseEntity<List<ReminderResponse>> getPendingReminders(
            @AuthenticationPrincipal User user) {
        List<Reminder> reminders = reminderRepository.findPendingReminders(
                user.getId(), LocalDateTime.now());
        List<ReminderResponse> response = reminders.stream()
                .map(this::toReminderResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reminders/{id}/snooze")
    @Operation(summary = "Snooze a reminder")
    public ResponseEntity<ReminderResponse> snoozeReminder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam(defaultValue = "15") int minutes) {
        Reminder reminder = notificationService.snoozeReminder(id, user.getId(), minutes);
        return ResponseEntity.ok(toReminderResponse(reminder));
    }
    
    @PostMapping("/reminders/{id}/acknowledge")
    @Operation(summary = "Acknowledge a reminder")
    public ResponseEntity<Void> acknowledgeReminder(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        notificationService.acknowledgeReminder(id, user.getId());
        return ResponseEntity.ok().build();
    }
    
    private NotificationPreferencesResponse toResponse(NotificationPreferences prefs) {
        return NotificationPreferencesResponse.builder()
                .emailNotificationsEnabled(prefs.isEmailNotificationsEnabled())
                .popupNotificationsEnabled(prefs.isPopupNotificationsEnabled())
                .dailyDigestEnabled(prefs.isDailyDigestEnabled())
                .dailyDigestTime(prefs.getDailyDigestTime())
                .quietHoursEnabled(prefs.isQuietHoursEnabled())
                .quietHoursStart(prefs.getQuietHoursStart())
                .quietHoursEnd(prefs.getQuietHoursEnd())
                .build();
    }
    
    private ReminderResponse toReminderResponse(Reminder reminder) {
        return ReminderResponse.builder()
                .id(reminder.getId())
                .taskId(reminder.getTask().getId())
                .taskTitle(reminder.getTask().getTitle())
                .reminderType(reminder.getReminderType().name())
                .reminderTime(reminder.getReminderTime())
                .sent(reminder.isSent())
                .snoozedUntil(reminder.getSnoozedUntil())
                .build();
    }
    
    @Data
    @Builder
    public static class NotificationPreferencesResponse {
        private boolean emailNotificationsEnabled;
        private boolean popupNotificationsEnabled;
        private boolean dailyDigestEnabled;
        private LocalTime dailyDigestTime;
        private boolean quietHoursEnabled;
        private LocalTime quietHoursStart;
        private LocalTime quietHoursEnd;
    }
    
    @Data
    public static class UpdatePreferencesRequest {
        private Boolean emailNotificationsEnabled;
        private Boolean popupNotificationsEnabled;
        private Boolean dailyDigestEnabled;
        private LocalTime dailyDigestTime;
        private Boolean quietHoursEnabled;
        private LocalTime quietHoursStart;
        private LocalTime quietHoursEnd;
    }
    
    @Data
    @Builder
    public static class ReminderResponse {
        private Long id;
        private Long taskId;
        private String taskTitle;
        private String reminderType;
        private LocalDateTime reminderTime;
        private boolean sent;
        private LocalDateTime snoozedUntil;
    }
}
