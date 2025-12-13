package com.taskmanager.strategy;

import com.taskmanager.dto.TaskDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy Pattern Implementation: Daily Calendar View
 * Shows tasks for a single day
 */
@Component
public class DailyCalendarView implements CalendarViewStrategy {
    
    private static final DateTimeFormatter LABEL_FORMATTER = 
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    
    @Override
    public String getViewType() {
        return "DAILY";
    }
    
    @Override
    public Map<LocalDate, List<TaskDto.TaskListResponse>> getTasksForPeriod(
            List<TaskDto.TaskListResponse> tasks, 
            LocalDate referenceDate) {
        
        Map<LocalDate, List<TaskDto.TaskListResponse>> result = new LinkedHashMap<>();
        
        List<TaskDto.TaskListResponse> dayTasks = tasks.stream()
                .filter(task -> task.getDueDate() != null && 
                        task.getDueDate().toLocalDate().equals(referenceDate))
                .sorted(Comparator.comparing(TaskDto.TaskListResponse::getPriority)
                        .thenComparing(TaskDto.TaskListResponse::getDueDate))
                .collect(Collectors.toList());
        
        result.put(referenceDate, dayTasks);
        
        return result;
    }
    
    @Override
    public LocalDate getPeriodStart(LocalDate referenceDate) {
        return referenceDate;
    }
    
    @Override
    public LocalDate getPeriodEnd(LocalDate referenceDate) {
        return referenceDate;
    }
    
    @Override
    public CalendarViewMetadata getMetadata(LocalDate referenceDate) {
        Map<LocalDate, Integer> taskCount = new HashMap<>();
        taskCount.put(referenceDate, 0); // Will be updated by service
        
        return CalendarViewMetadata.builder()
                .viewType(getViewType())
                .periodStart(referenceDate)
                .periodEnd(referenceDate)
                .today(LocalDate.now())
                .periodLabel(referenceDate.format(LABEL_FORMATTER))
                .taskCountPerDay(taskCount)
                .build();
    }
}
