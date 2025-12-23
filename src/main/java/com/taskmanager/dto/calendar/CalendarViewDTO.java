package com.taskmanager.dto.calendar;

import com.taskmanager.dto.task.TaskDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarViewDTO {

    private String viewType; // DAILY, WEEKLY, MONTHLY
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<LocalDate, List<CalendarTaskDTO>> tasksByDate;
    private CalendarSummary summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarTaskDTO {
        private Long id;
        private String title;
        private String priority;
        private String status;
        private LocalDate dueDate;
        private String dueTime;
        private Integer estimatedDuration;
        private String taskType;
        private Long projectId;
        private String projectName;
        private boolean overdue;
        private Long instanceId; // For recurring task instances
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CalendarSummary {
        private int totalTasks;
        private int completedTasks;
        private int overdueTasks;
        private Map<String, Integer> tasksByPriority;
    }
}
