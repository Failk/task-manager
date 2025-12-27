package com.taskmanager.service;

import com.taskmanager.dto.task.*;
import com.taskmanager.entity.*;
import com.taskmanager.entity.enums.TaskStatus;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.factory.TaskFactory;
import com.taskmanager.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final ContextRepository contextRepository;
    private final ProjectRepository projectRepository;
    private final TaskInstanceRepository taskInstanceRepository;
    private final TaskFactory taskFactory;
    private final UserService userService;
    private final AuditService auditService;
    private final RecurringTaskService recurringTaskService;

    @Transactional(readOnly = true)
    public List<TaskDTO> getAllTasks() {
        User user = userService.getCurrentUser();
        return taskRepository.findByUserIdOrderByDueDateAscPriorityAsc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksForToday() {
        User user = userService.getCurrentUser();
        LocalDate today = LocalDate.now();

        // Get one-time tasks for today
        List<TaskDTO> regularTasks = taskRepository.findByUserIdAndDueDate(user.getId(), today)
                .stream()
                .filter(task -> !(task instanceof RecurringTask)) // Exclude recurring parent tasks
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        // Get recurring task instances for today
        List<TaskDTO> recurringInstances = taskInstanceRepository.findByUserIdAndScheduledDate(user.getId(), today)
                .stream()
                .map(this::mapInstanceToDTO)
                .collect(Collectors.toList());

        // Combine both lists
        regularTasks.addAll(recurringInstances);
        return regularTasks;
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getOverdueTasks() {
        User user = userService.getCurrentUser();
        return taskRepository.findOverdueTasks(user.getId(), LocalDate.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long id) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
        return mapToDTO(task);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> filterTasks(TaskFilterRequest filter) {
        User user = userService.getCurrentUser();
        List<Task> tasks;

        if (filter.getKeyword() != null && !filter.getKeyword().isEmpty()) {
            tasks = taskRepository.searchByKeyword(user.getId(), filter.getKeyword());
        } else if (filter.getDueDateFrom() != null && filter.getDueDateTo() != null) {
            tasks = taskRepository.findByUserIdAndDueDateBetween(
                    user.getId(), filter.getDueDateFrom(), filter.getDueDateTo());
        } else {
            tasks = taskRepository.findByUserIdOrderByDueDateAscPriorityAsc(user.getId());
        }

        // Apply additional filters
        if (filter.getPriorities() != null && !filter.getPriorities().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> filter.getPriorities().contains(t.getPriority()))
                    .collect(Collectors.toList());
        }

        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> filter.getStatuses().contains(t.getStatus()))
                    .collect(Collectors.toList());
        }

        if (filter.getProjectId() != null) {
            tasks = tasks.stream()
                    .filter(t -> t.getProject() != null && t.getProject().getId().equals(filter.getProjectId()))
                    .collect(Collectors.toList());
        }

        if (filter.getContextIds() != null && !filter.getContextIds().isEmpty()) {
            tasks = tasks.stream()
                    .filter(t -> t.getContexts().stream()
                            .anyMatch(c -> filter.getContextIds().contains(c.getId())))
                    .collect(Collectors.toList());
        }

        if (Boolean.TRUE.equals(filter.getOverdue())) {
            tasks = tasks.stream()
                    .filter(Task::isOverdue)
                    .collect(Collectors.toList());
        }

        return tasks.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public TaskDTO createOneTimeTask(CreateOneTimeTaskRequest request) {
        User user = userService.getCurrentUser();
        OneTimeTask task = taskFactory.createOneTimeTask(request, user);
        task = (OneTimeTask) taskRepository.save(task);

        log.info("One-time task created: {} by user: {}", task.getTitle(), user.getEmail());
        auditService.logAction("Task", task.getId(), "CREATE", null, task.getTitle());

        return mapToDTO(task);
    }

    @Transactional
    public TaskDTO createRecurringTask(CreateRecurringTaskRequest request) {
        User user = userService.getCurrentUser();
        RecurringTask task = taskFactory.createRecurringTask(request, user);
        task = (RecurringTask) taskRepository.save(task);

        // Generate initial task instances
        recurringTaskService.generateTaskInstances(task);

        log.info("Recurring task created: {} by user: {}", task.getTitle(), user.getEmail());
        auditService.logAction("Task", task.getId(), "CREATE", null, task.getTitle());

        return mapToDTO(task);
    }

    @Transactional
    public TaskDTO updateTask(Long id, UpdateTaskRequest request) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        String oldTitle = task.getTitle();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
            if (request.getStatus() == TaskStatus.COMPLETED) {
                task.markAsCompleted();
            }
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getDueTime() != null) {
            task.setDueTime(request.getDueTime());
        }
        if (request.getEstimatedDuration() != null) {
            task.setEstimatedDuration(request.getEstimatedDuration());
        }
        if (request.getProjectId() != null) {
            Project project = projectRepository.findByIdAndUserId(request.getProjectId(), user.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));
            task.setProject(project);
        }
        if (request.getContextIds() != null) {
            Set<Context> contexts = new HashSet<>(contextRepository.findAllById(request.getContextIds()));
            task.setContexts(contexts);
        }

        // Handle reminders update
        if (request.getReminders() != null) {
            // Clear existing reminders
            task.getReminders().clear();

            // Add new reminders
            for (UpdateTaskRequest.ReminderRequest reminderReq : request.getReminders()) {
                Reminder reminder = createReminderForUpdate(reminderReq, task);
                task.getReminders().add(reminder);
            }
        } else if (request.getDueDate() != null || request.getDueTime() != null) {
            // Recalculate existing reminder times when due date/time changes
            for (Reminder reminder : task.getReminders()) {
                if (reminder.getLeadTimeMinutes() != null && task.getDueDate() != null) {
                    java.time.LocalDateTime dueDateTime = task.getDueTime() != null
                            ? java.time.LocalDateTime.of(task.getDueDate(), task.getDueTime())
                            : task.getDueDate().atStartOfDay();
                    reminder.setRemindAt(dueDateTime.minusMinutes(reminder.getLeadTimeMinutes()));
                    reminder.setSent(false); // Reset sent status
                }
            }
        }

        task = taskRepository.save(task);
        log.info("Task updated: {} by user: {}", task.getTitle(), user.getEmail());
        auditService.logAction("Task", task.getId(), "UPDATE", oldTitle, task.getTitle());

        return mapToDTO(task);
    }

    private Reminder createReminderForUpdate(UpdateTaskRequest.ReminderRequest request, Task task) {
        java.time.LocalDateTime remindAt;
        Integer leadMinutes = request.getLeadTimeMinutes() != null ? request.getLeadTimeMinutes() : 0;

        if (task.getDueDate() != null) {
            java.time.LocalDateTime dueDateTime = task.getDueTime() != null
                    ? java.time.LocalDateTime.of(task.getDueDate(), task.getDueTime())
                    : task.getDueDate().atStartOfDay();
            remindAt = dueDateTime.minusMinutes(leadMinutes);
        } else {
            remindAt = java.time.LocalDateTime.now();
        }

        return Reminder.builder()
                .task(task)
                .leadTimeMinutes(leadMinutes)
                .remindAt(remindAt)
                .notificationType(request.getNotificationType() != null
                        ? com.taskmanager.entity.enums.NotificationType.valueOf(request.getNotificationType())
                        : com.taskmanager.entity.enums.NotificationType.POPUP)
                .build();
    }

    @Transactional
    public TaskDTO completeTask(Long id) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        task.markAsCompleted();
        task = taskRepository.save(task);

        log.info("Task completed: {} by user: {}", task.getTitle(), user.getEmail());
        auditService.logAction("Task", task.getId(), "COMPLETE", "status=IN_PROGRESS", "status=COMPLETED");

        return mapToDTO(task);
    }

    @Transactional
    public TaskDTO updateTaskStatus(Long id, TaskStatus status) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        String oldStatus = task.getStatus().name();
        task.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            task.markAsCompleted();
        }

        task = taskRepository.save(task);
        log.info("Task status updated: {} to {} by user: {}", task.getTitle(), status, user.getEmail());
        auditService.logAction("Task", task.getId(), "UPDATE_STATUS", "status=" + oldStatus, "status=" + status);

        return mapToDTO(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));

        auditService.logAction("Task", task.getId(), "DELETE", task.getTitle(), null);
        taskRepository.delete(task);
        log.info("Task deleted: {} by user: {}", task.getTitle(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProject(Long projectId) {
        User user = userService.getCurrentUser();
        // Verify project belongs to user
        if (!projectRepository.existsByIdAndUserId(projectId, user.getId())) {
            throw new ResourceNotFoundException("Project", "id", projectId);
        }
        return taskRepository.findByProjectId(projectId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByContext(Long contextId) {
        User user = userService.getCurrentUser();
        return taskRepository.findByUserIdAndContextId(user.getId(), contextId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private TaskDTO mapToDTO(Task task) {
        TaskDTO.TaskDTOBuilder builder = TaskDTO.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .dueTime(task.getDueTime())
                .estimatedDuration(task.getEstimatedDuration())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .taskType(task.getTaskType())
                .overdue(task.isOverdue());

        if (task.getProject() != null) {
            builder.projectId(task.getProject().getId())
                    .projectName(task.getProject().getName());
        }

        if (task.getContexts() != null) {
            builder.contexts(task.getContexts().stream()
                    .map(c -> TaskDTO.ContextDTO.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (task.getReminders() != null) {
            builder.reminders(task.getReminders().stream()
                    .map(r -> TaskDTO.ReminderDTO.builder()
                            .id(r.getId())
                            .remindAt(r.getRemindAt())
                            .leadTimeMinutes(r.getLeadTimeMinutes())
                            .notificationType(r.getNotificationType().name())
                            .sent(r.isSent())
                            .acknowledged(r.isAcknowledged())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            if (recurringTask.getRecurrencePattern() != null) {
                RecurrencePattern pattern = recurringTask.getRecurrencePattern();
                builder.recurrencePattern(TaskDTO.RecurrencePatternDTO.builder()
                        .frequency(pattern.getFrequency().name())
                        .interval(pattern.getInterval())
                        .daysOfWeek(pattern.getDaysOfWeek() != null
                                ? pattern.getDaysOfWeek().stream().map(Enum::name).collect(Collectors.toList())
                                : null)
                        .dayOfMonth(pattern.getDayOfMonth())
                        .startDate(pattern.getStartDate())
                        .endDate(pattern.getEndDate())
                        .occurrences(pattern.getOccurrences())
                        .build());
            }
        }

        return builder.build();
    }

    private TaskDTO mapInstanceToDTO(TaskInstance instance) {
        Task task = instance.getRecurringTask();
        TaskDTO.TaskDTOBuilder builder = TaskDTO.builder()
                .id(task.getId())
                .instanceId(instance.getId()) // Include instance ID for status updates
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(instance.getStatus()) // Use instance status, not parent status
                .dueDate(instance.getScheduledDate()) // Use scheduled date
                .dueTime(instance.getScheduledTime() != null ? instance.getScheduledTime() : task.getDueTime())
                .estimatedDuration(task.getEstimatedDuration())
                .completedAt(instance.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .taskType("RECURRING_INSTANCE") // Mark as instance
                .overdue(instance.isOverdue());

        if (task.getProject() != null) {
            builder.projectId(task.getProject().getId())
                    .projectName(task.getProject().getName());
        }

        if (task.getContexts() != null) {
            builder.contexts(task.getContexts().stream()
                    .map(c -> TaskDTO.ContextDTO.builder()
                            .id(c.getId())
                            .name(c.getName())
                            .build())
                    .collect(Collectors.toList()));
        }

        return builder.build();
    }

    @Transactional
    public void updateTaskInstanceStatus(Long instanceId, TaskStatus status) {
        User user = userService.getCurrentUser();
        TaskInstance instance = taskInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new ResourceNotFoundException("TaskInstance", "id", instanceId));

        // Verify user owns this task
        if (!instance.getRecurringTask().getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("TaskInstance", "id", instanceId);
        }

        instance.setStatus(status);
        if (status == TaskStatus.COMPLETED) {
            instance.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            instance.setCompletedAt(null);
        }

        taskInstanceRepository.save(instance);
        log.info("Task instance {} status updated to {} by user {}", instanceId, status, user.getEmail());
    }
}
