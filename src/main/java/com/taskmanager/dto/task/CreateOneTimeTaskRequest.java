package com.taskmanager.dto.task;

import com.taskmanager.entity.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOneTimeTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private Priority priority;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer estimatedDuration;
    private Long projectId;
    private List<Long> contextIds;
    private List<ReminderRequest> reminders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderRequest {
        private Integer leadTimeMinutes;
        private String notificationType;
    }
}
