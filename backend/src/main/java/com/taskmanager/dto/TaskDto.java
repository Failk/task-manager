package com.taskmanager.dto;

import com.taskmanager.enums.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class TaskDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateOneTimeTaskRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        private String title;
        
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        private String description;
        
        @NotNull(message = "Due date is required")
        private LocalDateTime dueDate;
        
        private Priority priority;
        private Long projectId;
        private List<String> contextTags;
        private Integer estimatedDurationMinutes;
        private List<ReminderRequest> reminders;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRecurringTaskRequest {
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        private String title;
        
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        private String description;
        
        @NotNull(message = "Start date is required")
        private LocalDateTime startDateTime;
        
        private Priority priority;
        private Long projectId;
        private List<String> contextTags;
        private Integer estimatedDurationMinutes;
        
        // Recurrence settings
        @NotNull(message = "Frequency is required")
        private Frequency frequency;
        
        private Integer interval; // Default 1
        private Set<DayOfWeek> daysOfWeek;
        private Integer dayOfMonth;
        private EndCondition endCondition;
        private Integer occurrenceCount;
        private LocalDate endDate;
        
        private List<ReminderRequest> reminders;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateTaskRequest {
        @Size(max = 200, message = "Title cannot exceed 200 characters")
        private String title;
        
        @Size(max = 2000, message = "Description cannot exceed 2000 characters")
        private String description;
        
        private LocalDateTime dueDate;
        private Priority priority;
        private TaskStatus status;
        private Long projectId;
        private List<String> contextTags;
        private Integer estimatedDurationMinutes;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReminderRequest {
        @NotNull(message = "Reminder type is required")
        private ReminderType type;
        
        @NotNull(message = "Lead time is required")
        private Long leadTimeMinutes; // Minutes before due date
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskResponse {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime dueDate;
        private Priority priority;
        private TaskStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime completedAt;
        private Integer estimatedDurationMinutes;
        private String taskType;
        private boolean overdue;
        
        private ProjectDto.ProjectListResponse project;
        private List<String> contextTags;
        private List<ReminderResponse> reminders;
        private RecurrencePatternResponse recurrencePattern;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskListResponse {
        private Long id;
        private String title;
        private LocalDateTime dueDate;
        private Priority priority;
        private TaskStatus status;
        private String taskType;
        private boolean overdue;
        private String projectName;
        private String projectColor;
        private List<String> contextTags;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReminderResponse {
        private Long id;
        private ReminderType type;
        private Long leadTimeMinutes;
        private LocalDateTime reminderTime;
        private boolean sent;
        private boolean acknowledged;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecurrencePatternResponse {
        private Frequency frequency;
        private Integer interval;
        private Set<DayOfWeek> daysOfWeek;
        private Integer dayOfMonth;
        private EndCondition endCondition;
        private Integer occurrenceCount;
        private LocalDate endDate;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskInstanceResponse {
        private Long id;
        private Long recurringTaskId;
        private String taskTitle;
        private LocalDate scheduledDate;
        private TaskStatus status;
        private LocalDateTime completedAt;
        private boolean skipped;
        private String overrideTitle;
        private String overrideDescription;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TaskFilterRequest {
        private Priority priority;
        private TaskStatus status;
        private String contextName;
        private Long projectId;
        private LocalDate startDate;
        private LocalDate endDate;
        private String searchTerm;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentRequest {
        @NotBlank(message = "Comment content is required")
        @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
        private String content;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CommentResponse {
        private Long id;
        private String content;
        private Long authorId;
        private String authorName;
        private LocalDateTime createdAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectListResponse {
        private Long id;
        private String name;
        private String colorCode;
    }
}
