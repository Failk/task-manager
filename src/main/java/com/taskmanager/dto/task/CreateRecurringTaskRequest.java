package com.taskmanager.dto.task;

import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.RecurrenceFrequency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecurringTaskRequest {

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    private Priority priority;
    private LocalTime dueTime;
    private Integer estimatedDuration;
    private Long projectId;
    private List<Long> contextIds;
    
    @NotNull(message = "Recurrence pattern is required")
    private RecurrencePatternRequest recurrencePattern;
    
    private List<CreateOneTimeTaskRequest.ReminderRequest> reminders;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecurrencePatternRequest {
        @NotNull(message = "Frequency is required")
        private RecurrenceFrequency frequency;
        
        private int interval = 1;
        private Set<DayOfWeek> daysOfWeek;
        private Integer dayOfMonth;
        
        @NotNull(message = "Start date is required")
        private LocalDate startDate;
        
        private LocalDate endDate;
        private Integer occurrences;
    }
}
