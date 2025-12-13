package com.taskmanager.strategy;

import com.taskmanager.dto.TaskDto;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Strategy Pattern Implementation: Weekly Calendar View
 * Shows tasks for a 7-day period (Monday to Sunday)
 */
@Component
public class WeeklyCalendarView implements CalendarViewStrategy {
    
    private static final DateTimeFormatter LABEL_FORMATTER = 
            DateTimeFormatter.ofPattern("MMM d");
    
    @Override
    public String getViewType() {
        return "WEEKLY";
    }
    
    @Override
    public Map<LocalDate, List<TaskDto.TaskListResponse>> getTasksForPeriod(
            List<TaskDto.TaskListResponse> tasks, 
            LocalDate referenceDate) {
        
        LocalDate weekStart = getPeriodStart(referenceDate);
        LocalDate weekEnd = getPeriodEnd(referenceDate);
        
        Map<LocalDate, List<TaskDto.TaskListResponse>> result = new LinkedHashMap<>();
        
        // Initialize all days of the week
        for (LocalDate date = weekStart; !date.isAfter(weekEnd); date = date.plusDays(1)) {
            result.put(date, new ArrayList<>());
        }
        
        // Distribute tasks to their respective days
        for (TaskDto.TaskListResponse task : tasks) {
            if (task.getDueDate() != null) {
                LocalDate taskDate = task.getDueDate().toLocalDate();
                if (!taskDate.isBefore(weekStart) && !taskDate.isAfter(weekEnd)) {
                    result.get(taskDate).add(task);
                }
            }
        }
        
        // Sort tasks within each day by priority then time
        result.forEach((date, dayTasks) -> 
            dayTasks.sort(Comparator.comparing(TaskDto.TaskListResponse::getPriority)
                    .thenComparing(TaskDto.TaskListResponse::getDueDate)));
        
        return result;
    }
    
    @Override
    public LocalDate getPeriodStart(LocalDate referenceDate) {
        return referenceDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }
    
    @Override
    public LocalDate getPeriodEnd(LocalDate referenceDate) {
        return referenceDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
    }
    
    @Override
    public CalendarViewMetadata getMetadata(LocalDate referenceDate) {
        LocalDate start = getPeriodStart(referenceDate);
        LocalDate end = getPeriodEnd(referenceDate);
        
        Map<LocalDate, Integer> taskCount = new LinkedHashMap<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            taskCount.put(date, 0); // Will be updated by service
        }
        
        String periodLabel = String.format("%s - %s, %d",
                start.format(LABEL_FORMATTER),
                end.format(LABEL_FORMATTER),
                start.getYear());
        
        return CalendarViewMetadata.builder()
                .viewType(getViewType())
                .periodStart(start)
                .periodEnd(end)
                .today(LocalDate.now())
                .periodLabel(periodLabel)
                .taskCountPerDay(taskCount)
                .build();
    }
}
