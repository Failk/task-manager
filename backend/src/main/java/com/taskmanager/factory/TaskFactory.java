package com.taskmanager.factory;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.*;
import com.taskmanager.enums.EndCondition;
import com.taskmanager.enums.Priority;
import com.taskmanager.enums.TaskStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Factory Pattern: Task Factory
 * Creates appropriate task instances based on type
 */
@Component
public class TaskFactory {
    
    /**
     * Create a one-time task from DTO
     */
    public OneTimeTask createOneTimeTask(TaskDto.CreateOneTimeTaskRequest request, Project project) {
        return OneTimeTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.C)
                .status(TaskStatus.NOT_STARTED)
                .project(project)
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .build();
    }
    
    /**
     * Create a recurring task from DTO
     */
    public RecurringTask createRecurringTask(TaskDto.CreateRecurringTaskRequest request, Project project) {
        RecurrencePattern pattern = RecurrencePattern.builder()
                .frequency(request.getFrequency())
                .interval(request.getInterval() != null ? request.getInterval() : 1)
                .daysOfWeek(request.getDaysOfWeek())
                .dayOfMonth(request.getDayOfMonth())
                .endCondition(request.getEndCondition() != null ? request.getEndCondition() : EndCondition.NEVER)
                .occurrenceCount(request.getOccurrenceCount())
                .endDate(request.getEndDate())
                .build();
        
        return RecurringTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getStartDateTime())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.C)
                .status(TaskStatus.NOT_STARTED)
                .project(project)
                .estimatedDurationMinutes(request.getEstimatedDurationMinutes())
                .recurrencePattern(pattern)
                .startDate(request.getStartDateTime().toLocalDate())
                .endDate(request.getEndDate())
                .build();
    }
    
    /**
     * Create a task instance for a recurring task
     */
    public TaskInstance createTaskInstance(RecurringTask recurringTask, LocalDate instanceDate) {
        return TaskInstance.builder()
                .recurringTask(recurringTask)
                .instanceDate(instanceDate)
                .instanceTime(recurringTask.getDueDate().toLocalTime())
                .status(TaskStatus.NOT_STARTED)
                .overridden(false)
                .skipped(false)
                .build();
    }
    
    /**
     * Create a reminder from DTO
     */
    public Reminder createReminder(TaskDto.ReminderRequest request, Task task) {
        return Reminder.builder()
                .task(task)
                .reminderType(request.getType())
                .leadTimeMinutes(request.getLeadTimeMinutes())
                .sent(false)
                .acknowledged(false)
                .build();
    }
}
