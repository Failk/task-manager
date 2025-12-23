package com.taskmanager.dto.task;

import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {

    private Long id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Integer estimatedDuration;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String taskType;
    private Long projectId;
    private String projectName;
    private List<ContextDTO> contexts;
    private List<ReminderDTO> reminders;
    private RecurrencePatternDTO recurrencePattern;
    private boolean overdue;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContextDTO {
        private Long id;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReminderDTO {
        private Long id;
        private LocalDateTime remindAt;
        private Integer leadTimeMinutes;
        private String notificationType;
        private boolean sent;
        private boolean acknowledged;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurrencePatternDTO {
        private String frequency;
        private int interval;
        private List<String> daysOfWeek;
        private Integer dayOfMonth;
        private LocalDate startDate;
        private LocalDate endDate;
        private Integer occurrences;
    }
}
