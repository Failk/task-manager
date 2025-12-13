package com.taskmanager.strategy;

import com.taskmanager.dto.TaskDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy Pattern Implementation: Monthly Calendar View
 * Shows tasks for an entire month
 */
@Component
public class MonthlyCalendarView implements CalendarViewStrategy {
    
    private static final DateTimeFormatter LABEL_FORMATTER = 
            DateTimeFormatter.ofPattern("MMMM yyyy");
    
    @Override
    public String getViewType() {
        return "MONTHLY";
    }
    
    @Override
    public Map<LocalDate, List<TaskDto.TaskListResponse>> getTasksForPeriod(
            List<TaskDto.TaskListResponse> tasks, 
            LocalDate referenceDate) {
        
        LocalDate monthStart = getPeriodStart(referenceDate);
        LocalDate monthEnd = getPeriodEnd(referenceDate);
        
        Map<LocalDate, List<TaskDto.TaskListResponse>> result = new LinkedHashMap<>();
        
        // Initialize all days of the month
        for (LocalDate date = monthStart; !date.isAfter(monthEnd); date = date.plusDays(1)) {
            result.put(date, new ArrayList<>());
        }
        
        // Distribute tasks to their respective days
        for (TaskDto.TaskListResponse task : tasks) {
            if (task.getDueDate() != null) {
                LocalDate taskDate = task.getDueDate().toLocalDate();
                if (!taskDate.isBefore(monthStart) && !taskDate.isAfter(monthEnd)) {
                    result.get(taskDate).add(task);
                }
            }
        }
        
        // Sort tasks within each day by priority
        result.forEach((date, dayTasks) -> 
            dayTasks.sort(Comparator.comparing(TaskDto.TaskListResponse::getPriority)
                    .thenComparing(TaskDto.TaskListResponse::getDueDate)));
        
        return result;
    }
    
    @Override
    public LocalDate getPeriodStart(LocalDate referenceDate) {
        return YearMonth.from(referenceDate).atDay(1);
    }
    
    @Override
    public LocalDate getPeriodEnd(LocalDate referenceDate) {
        return YearMonth.from(referenceDate).atEndOfMonth();
    }
    
    @Override
    public CalendarViewMetadata getMetadata(LocalDate referenceDate) {
        LocalDate start = getPeriodStart(referenceDate);
        LocalDate end = getPeriodEnd(referenceDate);
        
        Map<LocalDate, Integer> taskCount = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            taskCount.put(date, 0); // Will be updated by service
        }
        
        return CalendarViewMetadata.builder()
                .viewType(getViewType())
                .periodStart(start)
                .periodEnd(end)
                .today(LocalDate.now())
                .periodLabel(referenceDate.format(LABEL_FORMATTER))
                .taskCountPerDay(taskCount)
                .build();
    }
}
