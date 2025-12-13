package com.taskmanager.service;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.Task;
import com.taskmanager.strategy.CalendarViewMetadata;
import com.taskmanager.strategy.CalendarViewStrategy;
import com.taskmanager.strategy.DailyCalendarView;
import com.taskmanager.strategy.MonthlyCalendarView;
import com.taskmanager.strategy.WeeklyCalendarView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CalendarService {
    
    private final TaskService taskService;
    private final DailyCalendarView dailyCalendarView;
    private final WeeklyCalendarView weeklyCalendarView;
    private final MonthlyCalendarView monthlyCalendarView;
    
    /**
     * Get calendar view using Strategy Pattern
     * Returns a Map.Entry with metadata as key and organized tasks as value
     */
    public Map.Entry<CalendarViewMetadata, Map<LocalDate, List<TaskDto.TaskListResponse>>> getCalendarView(
            Long userId, LocalDate referenceDate, String viewType) {
        
        CalendarViewStrategy strategy = getStrategy(viewType);
        
        LocalDate start = strategy.getPeriodStart(referenceDate);
        LocalDate end = strategy.getPeriodEnd(referenceDate);
        
        // Fetch tasks for the period
        List<Task> tasks = taskService.findByDateRange(
                userId,
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay().minusNanos(1)
        );
        
        List<TaskDto.TaskListResponse> taskResponses = taskService.toListResponse(tasks);
        
        // Organize tasks using the strategy
        Map<LocalDate, List<TaskDto.TaskListResponse>> organizedTasks = 
                strategy.getTasksForPeriod(taskResponses, referenceDate);
        
        // Get metadata and update task counts
        CalendarViewMetadata metadata = strategy.getMetadata(referenceDate);
        Map<LocalDate, Integer> taskCounts = new HashMap<>();
        organizedTasks.forEach((date, dayTasks) -> taskCounts.put(date, dayTasks.size()));
        metadata.setTaskCountPerDay(taskCounts);
        
        return new AbstractMap.SimpleEntry<>(metadata, organizedTasks);
    }
    
    public Map.Entry<CalendarViewMetadata, Map<LocalDate, List<TaskDto.TaskListResponse>>> getDailyView(
            Long userId, LocalDate date) {
        return getCalendarView(userId, date, "daily");
    }
    
    public Map.Entry<CalendarViewMetadata, Map<LocalDate, List<TaskDto.TaskListResponse>>> getWeeklyView(
            Long userId, LocalDate date) {
        return getCalendarView(userId, date, "weekly");
    }
    
    public Map.Entry<CalendarViewMetadata, Map<LocalDate, List<TaskDto.TaskListResponse>>> getMonthlyView(
            Long userId, LocalDate date) {
        return getCalendarView(userId, date, "monthly");
    }
    
    private CalendarViewStrategy getStrategy(String viewType) {
        return switch (viewType.toLowerCase()) {
            case "daily" -> dailyCalendarView;
            case "weekly" -> weeklyCalendarView;
            case "monthly" -> monthlyCalendarView;
            default -> weeklyCalendarView;
        };
    }
}
