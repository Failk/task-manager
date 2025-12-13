package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

public class ProjectDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Project name is required")
        @Size(max = 100, message = "Project name cannot exceed 100 characters")
        private String name;
        
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;
        
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
        private String colorCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        @Size(max = 100, message = "Project name cannot exceed 100 characters")
        private String name;
        
        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        private String description;
        
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Color must be a valid hex color code")
        private String colorCode;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectResponse {
        private Long id;
        private String name;
        private String description;
        private String colorCode;
        private LocalDateTime createdAt;
        private boolean archived;
        private int taskCount;
        private int completedTaskCount;
        private double completionPercentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectListResponse {
        private Long id;
        private String name;
        private String colorCode;
        private boolean archived;
        private int taskCount;
        private double completionPercentage;
    }
}
