package com.taskmanager.service;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.*;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.factory.TaskFactory;
import com.taskmanager.repository.ContextRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    
    private final TaskRepository taskRepository;
    private final ContextRepository contextRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final TaskFactory taskFactory;
    private final AuditService auditService;
    private final EntityManager entityManager;
    
    @Transactional
    public Task createOneTimeTask(Long userId, TaskDto.CreateOneTimeTaskRequest request) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectService.findByIdAndUserId(request.getProjectId(), userId);
        }
        
        OneTimeTask task = taskFactory.createOneTimeTask(request, project);
        
        // Add context tags
        if (request.getContextTags() != null) {
            addContextsToTask(task, request.getContextTags(), userId);
        }
        
        // Add reminders
        if (request.getReminders() != null) {
            for (TaskDto.ReminderRequest reminderRequest : request.getReminders()) {
                Reminder reminder = taskFactory.createReminder(reminderRequest, task);
                task.addReminder(reminder);
            }
        }
        
        Task saved = taskRepository.save(task);
        auditService.logAction(userId, "CREATE", "TASK", saved.getId(), "Created task: " + saved.getTitle());
        
        return saved;
    }
    
    @Transactional
    public Task createRecurringTask(Long userId, TaskDto.CreateRecurringTaskRequest request) {
        Project project = null;
        if (request.getProjectId() != null) {
            project = projectService.findByIdAndUserId(request.getProjectId(), userId);
        }
        
        RecurringTask task = taskFactory.createRecurringTask(request, project);
        
        // Add context tags
        if (request.getContextTags() != null) {
            addContextsToTask(task, request.getContextTags(), userId);
        }
        
        // Add reminders
        if (request.getReminders() != null) {
            for (TaskDto.ReminderRequest reminderRequest : request.getReminders()) {
                Reminder reminder = taskFactory.createReminder(reminderRequest, task);
                task.addReminder(reminder);
            }
        }
        
        // Generate initial task instances (3 months worth)
        task.generateInstances(90);
        
        Task saved = taskRepository.save(task);
        auditService.logAction(userId, "CREATE", "TASK", saved.getId(), "Created recurring task: " + saved.getTitle());
        
        return saved;
    }
    
    private void addContextsToTask(Task task, List<String> contextNames, Long userId) {
        for (String contextName : contextNames) {
            Context context = contextRepository.findByName(contextName)
                    .orElseGet(() -> {
                        Context newContext = Context.builder()
                                .name(contextName)
                                .build();
                        return contextRepository.save(newContext);
                    });
            task.addContext(context);
        }
    }
    
    public Task findByIdAndUserId(Long taskId, Long userId) {
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    }
    
    public List<Task> findAllByUserId(Long userId) {
        return taskRepository.findAllByUserId(userId);
    }
    
    public List<Task> findByProjectIdAndUserId(Long projectId, Long userId) {
        return taskRepository.findByProjectIdAndUserId(projectId, userId);
    }
    
    public List<Task> findByDateRange(Long userId, LocalDateTime start, LocalDateTime end) {
        return taskRepository.findByUserIdAndDueDateBetween(userId, start, end);
    }
    
    public List<Task> findOverdueTasks(Long userId) {
        return taskRepository.findOverdueTasks(userId, LocalDateTime.now());
    }
    
    public List<Task> searchTasks(Long userId, String query) {
        return taskRepository.searchByTitleOrDescription(userId, query);
    }
    
    @Transactional
    public Task updateTask(Long taskId, Long userId, TaskDto.UpdateTaskRequest request) {
        Task task = findByIdAndUserId(taskId, userId);
        
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            task.updateStatus(request.getStatus());
        }
        if (request.getEstimatedDurationMinutes() != null) {
            task.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        }
        if (request.getProjectId() != null) {
            Project project = projectService.findByIdAndUserId(request.getProjectId(), userId);
            task.setProject(project);
        }
        if (request.getContextTags() != null) {
            task.getContexts().clear();
            addContextsToTask(task, request.getContextTags(), userId);
        }
        
        Task saved = taskRepository.save(task);
        auditService.logAction(userId, "UPDATE", "TASK", saved.getId(), "Updated task: " + saved.getTitle());
        
        return saved;
    }
    
    @Transactional
    public Task completeTask(Long taskId, Long userId) {
        Task task = findByIdAndUserId(taskId, userId);
        task.markComplete();
        Task saved = taskRepository.save(task);
        auditService.logAction(userId, "COMPLETE", "TASK", saved.getId(), "Completed task: " + saved.getTitle());
        return saved;
    }
    
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = findByIdAndUserId(taskId, userId);
        String taskTitle = task.getTitle();
        taskRepository.delete(task);
        auditService.logAction(userId, "DELETE", "TASK", taskId, "Deleted task: " + taskTitle);
    }
    
    public TaskDto.TaskResponse toResponse(Task task) {
        TaskDto.TaskResponse.TaskResponseBuilder builder = TaskDto.TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .estimatedDurationMinutes(task.getEstimatedDurationMinutes())
                .taskType(task.getTaskType())
                .overdue(task.isOverdue())
                .contextTags(task.getContexts().stream()
                        .map(Context::getName)
                        .collect(Collectors.toList()))
                .reminders(task.getReminders().stream()
                        .map(this::toReminderResponse)
                        .collect(Collectors.toList()));
        
        if (task.getProject() != null) {
            builder.project(TaskDto.ProjectListResponse.builder()
                    .id(task.getProject().getId())
                    .name(task.getProject().getName())
                    .colorCode(task.getProject().getColorCode())
                    .build());
        }
        
        if (task instanceof RecurringTask) {
            RecurringTask recurringTask = (RecurringTask) task;
            if (recurringTask.getRecurrencePattern() != null) {
                builder.recurrencePattern(toRecurrencePatternResponse(recurringTask.getRecurrencePattern()));
            }
        }
        
        return builder.build();
    }
    
    public TaskDto.TaskListResponse toListResponse(Task task) {
        return TaskDto.TaskListResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .taskType(task.getTaskType())
                .overdue(task.isOverdue())
                .projectName(task.getProject() != null ? task.getProject().getName() : null)
                .projectColor(task.getProject() != null ? task.getProject().getColorCode() : null)
                .contextTags(task.getContexts().stream()
                        .map(Context::getName)
                        .collect(Collectors.toList()))
                .build();
    }
    
    public List<TaskDto.TaskListResponse> toListResponse(List<Task> tasks) {
        return tasks.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
    
    // Alias methods for controller
    public Task findByIdAndUser(Long taskId, Long userId) {
        return findByIdAndUserId(taskId, userId);
    }
    
    public Task update(Long taskId, Long userId, TaskDto.UpdateTaskRequest request) {
        return updateTask(taskId, userId, request);
    }
    
    public Task complete(Long taskId, Long userId) {
        return completeTask(taskId, userId);
    }
    
    public void delete(Long taskId, Long userId) {
        deleteTask(taskId, userId);
    }
    
    public List<Task> findOverdue(Long userId) {
        return findOverdueTasks(userId);
    }
    
    public List<Task> findByDate(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
        return taskRepository.findByUserIdAndDueDateBetween(userId, startOfDay, endOfDay);
    }
    
    @Transactional
    public Task defer(Long taskId, Long userId, LocalDate newDate) {
        Task task = findByIdAndUserId(taskId, userId);
        task.setDueDate(newDate.atStartOfDay());
        task.updateStatus(TaskStatus.DEFERRED);
        Task saved = taskRepository.save(task);
        auditService.logAction(userId, "DEFER", "TASK", saved.getId(), 
                "Deferred task to: " + newDate);
        return saved;
    }
    
    public List<Task> findByUserWithFilters(Long userId, TaskDto.TaskFilterRequest filter) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Task> query = cb.createQuery(Task.class);
        Root<Task> task = query.from(Task.class);
        
        List<Predicate> predicates = new ArrayList<>();
        
        // Join to project to filter by user
        Join<Task, Project> projectJoin = task.join("project", JoinType.LEFT);
        predicates.add(cb.equal(projectJoin.get("user").get("id"), userId));
        
        if (filter.getProjectId() != null) {
            predicates.add(cb.equal(projectJoin.get("id"), filter.getProjectId()));
        }
        
        if (filter.getPriority() != null) {
            predicates.add(cb.equal(task.get("priority"), filter.getPriority()));
        }
        
        if (filter.getStatus() != null) {
            predicates.add(cb.equal(task.get("status"), filter.getStatus()));
        }
        
        if (filter.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(task.get("dueDate"), 
                    filter.getStartDate().atStartOfDay()));
        }
        
        if (filter.getEndDate() != null) {
            predicates.add(cb.lessThan(task.get("dueDate"), 
                    filter.getEndDate().plusDays(1).atStartOfDay()));
        }
        
        if (filter.getSearchTerm() != null && !filter.getSearchTerm().isBlank()) {
            String searchPattern = "%" + filter.getSearchTerm().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(task.get("title")), searchPattern),
                    cb.like(cb.lower(task.get("description")), searchPattern)
            ));
        }
        
        if (filter.getContextName() != null && !filter.getContextName().isBlank()) {
            Join<Task, Context> contextJoin = task.join("contexts", JoinType.INNER);
            predicates.add(cb.equal(contextJoin.get("name"), filter.getContextName()));
        }
        
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(task.get("dueDate")));
        
        return entityManager.createQuery(query).getResultList();
    }
    
    // Task Instance methods for recurring tasks
    public List<TaskInstance> findInstancesByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT ti FROM TaskInstance ti " +
                      "JOIN ti.recurringTask rt " +
                      "JOIN rt.project p " +
                      "WHERE p.user.id = :userId " +
                      "AND ti.scheduledDate >= :startDate " +
                      "AND ti.scheduledDate <= :endDate " +
                      "ORDER BY ti.scheduledDate";
        
        TypedQuery<TaskInstance> query = entityManager.createQuery(jpql, TaskInstance.class);
        query.setParameter("userId", userId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        
        return query.getResultList();
    }
    
    @Transactional
    public TaskInstance completeInstance(Long instanceId, Long userId) {
        TaskInstance instance = findInstanceByIdAndUser(instanceId, userId);
        instance.markComplete();
        return entityManager.merge(instance);
    }
    
    @Transactional
    public TaskInstance skipInstance(Long instanceId, Long userId) {
        TaskInstance instance = findInstanceByIdAndUser(instanceId, userId);
        instance.skip();
        return entityManager.merge(instance);
    }
    
    private TaskInstance findInstanceByIdAndUser(Long instanceId, Long userId) {
        String jpql = "SELECT ti FROM TaskInstance ti " +
                      "JOIN ti.recurringTask rt " +
                      "JOIN rt.project p " +
                      "WHERE ti.id = :instanceId " +
                      "AND p.user.id = :userId";
        
        TypedQuery<TaskInstance> query = entityManager.createQuery(jpql, TaskInstance.class);
        query.setParameter("instanceId", instanceId);
        query.setParameter("userId", userId);
        
        List<TaskInstance> results = query.getResultList();
        if (results.isEmpty()) {
            throw new ResourceNotFoundException("Task instance not found");
        }
        return results.get(0);
    }
    
    public TaskDto.TaskInstanceResponse toInstanceResponse(TaskInstance instance) {
        return TaskDto.TaskInstanceResponse.builder()
                .id(instance.getId())
                .recurringTaskId(instance.getRecurringTask().getId())
                .taskTitle(instance.getRecurringTask().getTitle())
                .scheduledDate(instance.getScheduledDate())
                .status(instance.getStatus())
                .completedAt(instance.getCompletedAt())
                .skipped(instance.isSkipped())
                .overrideTitle(instance.getOverrideTitle())
                .overrideDescription(instance.getOverrideDescription())
                .build();
    }
    
    // Comment methods
    @Transactional
    public TaskComment addComment(Long taskId, Long userId, TaskDto.CommentRequest request) {
        Task task = findByIdAndUserId(taskId, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        TaskComment comment = TaskComment.builder()
                .task(task)
                .user(user)
                .content(request.getContent())
                .build();
        
        task.getComments().add(comment);
        taskRepository.save(task);
        
        return comment;
    }
    
    public List<TaskComment> getComments(Long taskId, Long userId) {
        Task task = findByIdAndUserId(taskId, userId);
        return new ArrayList<>(task.getComments());
    }
    
    public TaskDto.CommentResponse toCommentResponse(TaskComment comment) {
        return TaskDto.CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getUser().getId())
                .authorName(comment.getUser().getFullName())
                .createdAt(comment.getCreatedAt())
                .build();
    }
    
    // Context methods
    @Transactional
    public Task addContext(Long taskId, Long userId, Long contextId) {
        Task task = findByIdAndUserId(taskId, userId);
        Context context = contextRepository.findById(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("Context not found"));
        
        task.addContext(context);
        return taskRepository.save(task);
    }
    
    @Transactional
    public Task removeContext(Long taskId, Long userId, Long contextId) {
        Task task = findByIdAndUserId(taskId, userId);
        Context context = contextRepository.findById(contextId)
                .orElseThrow(() -> new ResourceNotFoundException("Context not found"));
        
        task.removeContext(context);
        return taskRepository.save(task);
    }
    
    private TaskDto.ReminderResponse toReminderResponse(Reminder reminder) {
        return TaskDto.ReminderResponse.builder()
                .id(reminder.getId())
                .type(reminder.getReminderType())
                .leadTimeMinutes(reminder.getLeadTimeMinutes())
                .reminderTime(reminder.getReminderTime())
                .sent(reminder.isSent())
                .acknowledged(reminder.isAcknowledged())
                .build();
    }
    
    private TaskDto.RecurrencePatternResponse toRecurrencePatternResponse(RecurrencePattern pattern) {
        return TaskDto.RecurrencePatternResponse.builder()
                .frequency(pattern.getFrequency())
                .interval(pattern.getInterval())
                .daysOfWeek(pattern.getDaysOfWeek())
                .dayOfMonth(pattern.getDayOfMonth())
                .endCondition(pattern.getEndCondition())
                .occurrenceCount(pattern.getOccurrenceCount())
                .endDate(pattern.getEndDate())
                .build();
    }
}
