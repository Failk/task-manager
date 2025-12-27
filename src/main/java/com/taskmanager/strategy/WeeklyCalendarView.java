package com.taskmanager.strategy;

import com.taskmanager.dto.calendar.CalendarViewDTO;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskInstance;
import com.taskmanager.entity.enums.TaskStatus;
import com.taskmanager.repository.TaskInstanceRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WeeklyCalendarView implements CalendarViewStrategy {

    private final TaskRepository taskRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    @Override
    public CalendarViewDTO generateView(Long userId, LocalDate referenceDate) {
        LocalDate date = referenceDate != null ? referenceDate : LocalDate.now();

        // Get start and end of the week (Monday to Sunday)
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        // Get tasks for the week
        List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, startOfWeek, endOfWeek);

        // Get recurring task instances for the week
        List<TaskInstance> instances = taskInstanceRepository.findByUserIdAndScheduledDateBetween(
                userId, startOfWeek, endOfWeek);

        Map<String, List<CalendarViewDTO.CalendarTaskDTO>> tasksByDate = new LinkedHashMap<>();
        List<CalendarViewDTO.CalendarTaskDTO> allTasks = new ArrayList<>();

        // Initialize all days of the week with ISO date string keys
        for (LocalDate d = startOfWeek; !d.isAfter(endOfWeek); d = d.plusDays(1)) {
            tasksByDate.put(d.toString(), new ArrayList<>());
        }

        // Add one-time tasks
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                CalendarViewDTO.CalendarTaskDTO dto = mapTaskToCalendarDTO(task, null);
                String dateKey = task.getDueDate().toString();
                if (tasksByDate.containsKey(dateKey)) {
                    tasksByDate.get(dateKey).add(dto);
                    allTasks.add(dto);
                }
            }
        }

        // Add recurring task instances
        for (TaskInstance instance : instances) {
            CalendarViewDTO.CalendarTaskDTO dto = mapTaskToCalendarDTO(instance.getRecurringTask(), instance);
            String dateKey = instance.getScheduledDate().toString();
            if (tasksByDate.containsKey(dateKey)) {
                tasksByDate.get(dateKey).add(dto);
                allTasks.add(dto);
            }
        }

        // Sort tasks within each day
        tasksByDate.values().forEach(dayTasks -> dayTasks.sort(Comparator.comparing(
                t -> t.getDueTime() != null ? t.getDueTime() : "23:59")));

        return CalendarViewDTO.builder()
                .viewType(getViewType())
                .startDate(startOfWeek)
                .endDate(endOfWeek)
                .tasksByDate(tasksByDate)
                .summary(calculateSummary(allTasks))
                .build();
    }

    @Override
    public String getViewType() {
        return "WEEKLY";
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
