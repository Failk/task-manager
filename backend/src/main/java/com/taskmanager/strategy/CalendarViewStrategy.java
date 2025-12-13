package com.taskmanager.strategy;

import com.taskmanager.dto.TaskDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Strategy Pattern: Calendar Interface
 * Defines the contract for different calendar view implementations
 */
public interface CalendarViewStrategy {
    
    /**
     * Get the type of calendar view
     */
    String getViewType();
    
    /**
     * Get tasks organized for this view type
     * @param tasks List of tasks to organize
     * @param referenceDate The date to center the view around
     * @return Map of dates to tasks for that date
     */
    Map<LocalDate, List<TaskDto.TaskListResponse>> getTasksForPeriod(
            List<TaskDto.TaskListResponse> tasks, 
            LocalDate referenceDate);
    
    /**
     * Get the start date of the current view period
     */
    LocalDate getPeriodStart(LocalDate referenceDate);
    
    /**
     * Get the end date of the current view period
     */
    LocalDate getPeriodEnd(LocalDate referenceDate);
    
    /**
     * Get view-specific metadata
     */
    CalendarViewMetadata getMetadata(LocalDate referenceDate);
}
