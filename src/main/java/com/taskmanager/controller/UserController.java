package com.taskmanager.controller;

import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.dto.notification.NotificationPreferencesDTO;
import com.taskmanager.dto.user.ChangePasswordRequest;
import com.taskmanager.dto.user.UpdateUserRequest;
import com.taskmanager.dto.user.UserDTO;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User profile and settings management APIs")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> getCurrentUser() {
        UserDTO user = userService.getCurrentUserDTO();
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @Operation(summary = "Update user profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    @Operation(summary = "Change password")
    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @Operation(summary = "Get notification preferences")
    @GetMapping("/me/notifications")
    public ResponseEntity<ApiResponse<NotificationPreferencesDTO>> getNotificationPreferences() {
        NotificationPreferencesDTO prefs = userService.getNotificationPreferences();
        return ResponseEntity.ok(ApiResponse.success(prefs));
    }

    @Operation(summary = "Update notification preferences")
    @PutMapping("/me/notifications")
    public ResponseEntity<ApiResponse<NotificationPreferencesDTO>> updateNotificationPreferences(
            @Valid @RequestBody NotificationPreferencesDTO request) {
        NotificationPreferencesDTO prefs = userService.updateNotificationPreferences(request);
        return ResponseEntity.ok(ApiResponse.success("Notification preferences updated", prefs));
    }
}
