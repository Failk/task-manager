package com.taskmanager.service;

import com.taskmanager.entity.RecurrencePattern;
import com.taskmanager.entity.RecurringTask;
import com.taskmanager.entity.TaskInstance;
import com.taskmanager.entity.enums.RecurrenceFrequency;
import com.taskmanager.repository.TaskInstanceRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringTaskService {

    private final TaskInstanceRepository taskInstanceRepository;
    private final TaskRepository taskRepository;
    
    private static final int DEFAULT_INSTANCES_TO_GENERATE = 30; // Generate 30 days worth

    @Transactional
    public void generateTaskInstances(RecurringTask task) {
        RecurrencePattern pattern = task.getRecurrencePattern();
        if (pattern == null) {
            return;
        }

        List<LocalDate> dates = calculateOccurrences(pattern, DEFAULT_INSTANCES_TO_GENERATE);
        List<TaskInstance> instances = new ArrayList<>();

        for (LocalDate date : dates) {
            // Check if instance already exists
            if (taskInstanceRepository.findByRecurringTaskIdAndScheduledDate(task.getId(), date).isEmpty()) {
                TaskInstance instance = TaskInstance.builder()
                        .recurringTask(task)
                        .scheduledDate(date)
                        .scheduledTime(task.getDueTime())
                        .build();
                instances.add(instance);
            }
        }

        if (!instances.isEmpty()) {
            taskInstanceRepository.saveAll(instances);
            log.info("Generated {} task instances for recurring task: {}", instances.size(), task.getTitle());
        }
    }

    public List<LocalDate> calculateOccurrences(RecurrencePattern pattern, int maxOccurrences) {
        List<LocalDate> occurrences = new ArrayList<>();
        LocalDate currentDate = pattern.getStartDate();
        LocalDate endDate = pattern.getEndDate() != null 
                ? pattern.getEndDate() 
                : LocalDate.now().plusMonths(3);
        
        int count = 0;
        int maxCount = pattern.getOccurrences() != null 
                ? Math.min(pattern.getOccurrences(), maxOccurrences)
                : maxOccurrences;

        while (!currentDate.isAfter(endDate) && count < maxCount) {
            if (isValidOccurrence(currentDate, pattern)) {
                occurrences.add(currentDate);
                count++;
            }
            currentDate = getNextDate(currentDate, pattern);
        }

        return occurrences;
    }

    private boolean isValidOccurrence(LocalDate date, RecurrencePattern pattern) {
        switch (pattern.getFrequency()) {
            case WEEKLY:
            case BIWEEKLY:
                Set<DayOfWeek> validDays = pattern.getDaysOfWeek();
                if (validDays != null && !validDays.isEmpty()) {
                    return validDays.contains(date.getDayOfWeek());
                }
                return true;
            case MONTHLY:
                if (pattern.getDayOfMonth() != null) {
                    return date.getDayOfMonth() == pattern.getDayOfMonth();
                }
                return true;
            default:
                return true;
        }
    }

    private LocalDate getNextDate(LocalDate current, RecurrencePattern pattern) {
        int interval = pattern.getInterval() > 0 ? pattern.getInterval() : 1;

        switch (pattern.getFrequency()) {
            case DAILY:
                return current.plusDays(interval);
            case WEEKLY:
                return current.plusDays(1); // Check each day for valid weekdays
            case BIWEEKLY:
                return current.plusDays(1); // Check each day for valid weekdays
            case MONTHLY:
                return current.plusMonths(interval);
            case YEARLY:
                return current.plusYears(interval);
            default:
                return current.plusDays(1);
        }
    }

    /**
     * Scheduled job to generate task instances for upcoming recurring tasks.
     * Runs daily at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void generateUpcomingTaskInstances() {
        log.info("Running scheduled task instance generation...");
        
        List<RecurringTask> recurringTasks = taskRepository.findAll().stream()
                .filter(task -> task instanceof RecurringTask)
                .map(task -> (RecurringTask) task)
                .toList();

        for (RecurringTask task : recurringTasks) {
            try {
                generateTaskInstances(task);
            } catch (Exception e) {
                log.error("Failed to generate instances for task: {}", task.getId(), e);
            }
        }

        log.info("Completed scheduled task instance generation for {} recurring tasks", recurringTasks.size());
    }
}
