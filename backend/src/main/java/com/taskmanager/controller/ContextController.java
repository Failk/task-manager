package com.taskmanager.controller;

import com.taskmanager.entity.Context;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ContextRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contexts")
@RequiredArgsConstructor
@Tag(name = "Contexts", description = "Context management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ContextController {
    
    private final ContextRepository contextRepository;
    
    @GetMapping
    @Operation(summary = "Get all contexts (default + user-specific)")
    public ResponseEntity<List<ContextResponse>> getAllContexts(
            @AuthenticationPrincipal User user) {
        List<Context> contexts = contextRepository.findAllAvailableForUser(user.getId());
        List<ContextResponse> response = contexts.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/default")
    @Operation(summary = "Get default contexts only")
    public ResponseEntity<List<ContextResponse>> getDefaultContexts() {
        List<Context> contexts = contextRepository.findDefaultContexts();
        List<ContextResponse> response = contexts.stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @Operation(summary = "Create a custom context")
    public ResponseEntity<ContextResponse> createContext(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CreateContextRequest request) {
        Context context = Context.builder()
                .name(request.getName())
                .description(request.getDescription())
                .color(request.getColor())
                .icon(request.getIcon())
                .isDefault(false)
                .user(user)
                .build();
        context = contextRepository.save(context);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(toResponse(context));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a custom context")
    public ResponseEntity<ContextResponse> updateContext(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UpdateContextRequest request) {
        Context context = contextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Context not found"));
        
        if (context.isDefault()) {
            throw new IllegalArgumentException("Cannot modify default contexts");
        }
        
        if (context.getUser() == null || !context.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Context not found");
        }
        
        if (request.getName() != null) {
            context.setName(request.getName());
        }
        if (request.getDescription() != null) {
            context.setDescription(request.getDescription());
        }
        if (request.getColor() != null) {
            context.setColor(request.getColor());
        }
        if (request.getIcon() != null) {
            context.setIcon(request.getIcon());
        }
        
        context = contextRepository.save(context);
        return ResponseEntity.ok(toResponse(context));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a custom context")
    public ResponseEntity<Void> deleteContext(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Context context = contextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Context not found"));
        
        if (context.isDefault()) {
            throw new IllegalArgumentException("Cannot delete default contexts");
        }
        
        if (context.getUser() == null || !context.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Context not found");
        }
        
        contextRepository.delete(context);
        return ResponseEntity.noContent().build();
    }
    
    private ContextResponse toResponse(Context context) {
        return ContextResponse.builder()
                .id(context.getId())
                .name(context.getName())
                .description(context.getDescription())
                .color(context.getColor())
                .icon(context.getIcon())
                .isDefault(context.isDefault())
                .build();
    }
    
    @Data
    public static class CreateContextRequest {
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        private String name;
        
        @Size(max = 200, message = "Description must not exceed 200 characters")
        private String description;
        
        private String color;
        private String icon;
    }
    
    @Data
    public static class UpdateContextRequest {
        @Size(max = 50, message = "Name must not exceed 50 characters")
        private String name;
        
        @Size(max = 200, message = "Description must not exceed 200 characters")
        private String description;
        
        private String color;
        private String icon;
    }
    
    @Data
    @Builder
    public static class ContextResponse {
        private Long id;
        private String name;
        private String description;
        private String color;
        private String icon;
        private boolean isDefault;
    }
}
