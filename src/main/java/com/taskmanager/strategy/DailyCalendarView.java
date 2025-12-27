package com.taskmanager.strategy;

import com.taskmanager.dto.calendar.CalendarViewDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskInstance;
import com.taskmanager.entity.enums.TaskStatus;
import com.taskmanager.repository.TaskInstanceRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DailyCalendarView implements CalendarViewStrategy {

    private final TaskRepository taskRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    @Override
    public CalendarViewDTO generateView(Long userId, LocalDate referenceDate) {
        LocalDate date = referenceDate != null ? referenceDate : LocalDate.now();

        // Get one-time tasks for the day
        List<Task> tasks = taskRepository.findByUserIdAndDueDate(userId, date);

        // Get recurring task instances for the day
        List<TaskInstance> instances = taskInstanceRepository.findByUserIdAndScheduledDate(userId, date);

        Map<String, List<CalendarViewDTO.CalendarTaskDTO>> tasksByDate = new HashMap<>();
        List<CalendarViewDTO.CalendarTaskDTO> dayTasks = new ArrayList<>();

        // Add one-time tasks
        for (Task task : tasks) {
            dayTasks.add(mapTaskToCalendarDTO(task, null));
        }

        // Add recurring task instances
        for (TaskInstance instance : instances) {
            dayTasks.add(mapTaskToCalendarDTO(instance.getRecurringTask(), instance));
        }

        // Sort by time
        dayTasks.sort(Comparator.comparing(
                t -> t.getDueTime() != null ? t.getDueTime() : "23:59"));

        tasksByDate.put(date.toString(), dayTasks);

        return CalendarViewDTO.builder()
                .viewType(getViewType())
                .startDate(date)
                .endDate(date)
                .tasksByDate(tasksByDate)
                .summary(calculateSummary(dayTasks))
                .build();
    }

    @Override
    public String getViewType() {
        return "DAILY";
    }

    private CalendarViewDTO.CalendarTaskDTO mapTaskToCalendarDTO(Task task, TaskInstance instance) {
        return CalendarViewDTO.CalendarTaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .priority(task.getPriority().name())
                .status(instance != null ? instance.getStatus().name() : task.getStatus().name())
                .dueDate(instance != null ? instance.getScheduledDate() : task.getDueDate())
                .dueTime(task.getDueTime() != null ? task.getDueTime().toString() : null)
                .estimatedDuration(task.getEstimatedDuration())
                .taskType(task.getTaskType())
                .projectId(task.getProject() != null ? task.getProject().getId() : null)
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .overdue(instance != null ? instance.isOverdue() : task.isOverdue())
                .instanceId(instance != null ? instance.getId() : null)
                .build();
    }

    private CalendarViewDTO.CalendarSummary calculateSummary(List<CalendarViewDTO.CalendarTaskDTO> tasks) {
        Map<String, Integer> byPriority = tasks.stream()
                .collect(Collectors.groupingBy(
                        CalendarViewDTO.CalendarTaskDTO::getPriority,
                        Collectors.summingInt(t -> 1)));

        return CalendarViewDTO.CalendarSummary.builder()
                .totalTasks(tasks.size())
                .completedTasks((int) tasks.stream()
                        .filter(t -> TaskStatus.COMPLETED.name().equals(t.getStatus()))
                        .count())
                .overdueTasks((int) tasks.stream()
                        .filter(CalendarViewDTO.CalendarTaskDTO::isOverdue)
                        .count())
                .tasksByPriority(byPriority)
                .build();
    }
}
