package com.taskmanager.controller;

import com.taskmanager.dto.UserDto;
import com.taskmanager.entity.User;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    public ResponseEntity<UserDto.UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.toResponse(user));
    }
    
    @PutMapping("/me")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<UserDto.UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDto.UpdateProfileRequest request) {
        User updated = userService.updateProfile(user.getId(), request);
        return ResponseEntity.ok(userService.toResponse(updated));
    }
    
    @PostMapping("/me/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UserDto.ChangePasswordRequest request) {
        userService.changePassword(user.getId(), request);
        return ResponseEntity.ok().build();
    }
}
