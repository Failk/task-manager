package com.taskmanager.factory;

import com.taskmanager.dto.task.CreateOneTimeTaskRequest;
import com.taskmanager.dto.task.CreateRecurringTaskRequest;
import com.taskmanager.entity.*;
import com.taskmanager.entity.enums.NotificationType;
import com.taskmanager.entity.enums.Priority;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ContextRepository;
import com.taskmanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TaskFactory {

    private final ProjectRepository projectRepository;
    private final ContextRepository contextRepository;

    public OneTimeTask createOneTimeTask(CreateOneTimeTaskRequest request, User user) {
        OneTimeTask task = OneTimeTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.C)
                .dueDate(request.getDueDate())
                .dueTime(request.getDueTime())
                .estimatedDuration(request.getEstimatedDuration())
                .user(user)
                .build();

        // Set project if specified
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndUserId(request.getProjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            task.setProject(project);
        }

        // Set contexts
        if (request.getContextIds() != null && !request.getContextIds().isEmpty()) {
            Set<Context> contexts = new HashSet<>(contextRepository.findAllById(request.getContextIds()));
            task.setContexts(contexts);
        }

        // Create reminders
        if (request.getReminders() != null) {
            List<Reminder> reminders = request.getReminders().stream()
                    .map(r -> createReminder(r, task))
                    .collect(Collectors.toList());
            task.setReminders(reminders);
        }

        return task;
    }

    public RecurringTask createRecurringTask(CreateRecurringTaskRequest request, User user) {
        RecurringTask task = RecurringTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : Priority.C)
                .dueTime(request.getDueTime())
                .estimatedDuration(request.getEstimatedDuration())
                .user(user)
                .build();

        // Set project if specified
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndUserId(request.getProjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            task.setProject(project);
        }

        // Set contexts
        if (request.getContextIds() != null && !request.getContextIds().isEmpty()) {
            Set<Context> contexts = new HashSet<>(contextRepository.findAllById(request.getContextIds()));
            task.setContexts(contexts);
        }

        // Create recurrence pattern
        CreateRecurringTaskRequest.RecurrencePatternRequest patternRequest = request.getRecurrencePattern();
        RecurrencePattern pattern = RecurrencePattern.builder()
                .frequency(patternRequest.getFrequency())
                .interval(patternRequest.getInterval())
                .daysOfWeek(patternRequest.getDaysOfWeek())
                .dayOfMonth(patternRequest.getDayOfMonth())
                .startDate(patternRequest.getStartDate())
                .endDate(patternRequest.getEndDate())
                .occurrences(patternRequest.getOccurrences())
                .build();
        
        task.setRecurrencePattern(pattern);
        task.setDueDate(patternRequest.getStartDate());

        // Create reminders
        if (request.getReminders() != null) {
            List<Reminder> reminders = request.getReminders().stream()
                    .map(r -> createReminder(r, task))
                    .collect(Collectors.toList());
            task.setReminders(reminders);
        }

        return task;
    }

    private Reminder createReminder(CreateOneTimeTaskRequest.ReminderRequest request, Task task) {
        LocalDateTime remindAt = null;
        if (task.getDueDate() != null && request.getLeadTimeMinutes() != null) {
            LocalDateTime dueDateTime = task.getDueTime() != null 
                    ? LocalDateTime.of(task.getDueDate(), task.getDueTime())
                    : task.getDueDate().atStartOfDay();
            remindAt = dueDateTime.minusMinutes(request.getLeadTimeMinutes());
        }

        return Reminder.builder()
                .task(task)
                .leadTimeMinutes(request.getLeadTimeMinutes())
                .remindAt(remindAt)
                .notificationType(request.getNotificationType() != null 
                        ? NotificationType.valueOf(request.getNotificationType())
                        : NotificationType.POPUP)
                .build();
    }
}
