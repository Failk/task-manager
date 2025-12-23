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
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MonthlyCalendarView implements CalendarViewStrategy {

    private final TaskRepository taskRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    @Override
    public CalendarViewDTO generateView(Long userId, LocalDate referenceDate) {
        LocalDate date = referenceDate != null ? referenceDate : LocalDate.now();
        
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // Get tasks for the month
        List<Task> tasks = taskRepository.findByUserIdAndDueDateBetween(userId, startOfMonth, endOfMonth);
        
        // Get recurring task instances for the month
        List<TaskInstance> instances = taskInstanceRepository.findByUserIdAndScheduledDateBetween(
                userId, startOfMonth, endOfMonth);

        Map<LocalDate, List<CalendarViewDTO.CalendarTaskDTO>> tasksByDate = new LinkedHashMap<>();
        List<CalendarViewDTO.CalendarTaskDTO> allTasks = new ArrayList<>();

        // Initialize all days of the month
        for (LocalDate d = startOfMonth; !d.isAfter(endOfMonth); d = d.plusDays(1)) {
            tasksByDate.put(d, new ArrayList<>());
        }

        // Add one-time tasks
        for (Task task : tasks) {
            if (task.getDueDate() != null) {
                CalendarViewDTO.CalendarTaskDTO dto = mapTaskToCalendarDTO(task, null);
                tasksByDate.get(task.getDueDate()).add(dto);
                allTasks.add(dto);
            }
        }

        // Add recurring task instances
        for (TaskInstance instance : instances) {
            CalendarViewDTO.CalendarTaskDTO dto = mapTaskToCalendarDTO(instance.getRecurringTask(), instance);
            tasksByDate.get(instance.getScheduledDate()).add(dto);
            allTasks.add(dto);
        }

        // Sort tasks within each day by priority
        tasksByDate.values().forEach(dayTasks -> 
                dayTasks.sort(Comparator.comparing(CalendarViewDTO.CalendarTaskDTO::getPriority)));

        return CalendarViewDTO.builder()
                .viewType(getViewType())
                .startDate(startOfMonth)
                .endDate(endOfMonth)
                .tasksByDate(tasksByDate)
                .summary(calculateSummary(allTasks))
                .build();
    }

    @Override
    public String getViewType() {
        return "MONTHLY";
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
