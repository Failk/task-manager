package com.taskmanager.controller;

import com.taskmanager.dto.TaskDto;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.TaskInstance;
import com.taskmanager.entity.User;
import com.taskmanager.enums.Priority;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    
    private final TaskService taskService;
    
    @GetMapping
    @Operation(summary = "Get all tasks with optional filters")
    public ResponseEntity<List<TaskDto.TaskListResponse>> getAllTasks(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String contextName,
            @RequestParam(required = false) String search) {
        
        TaskDto.TaskFilterRequest filter = TaskDto.TaskFilterRequest.builder()
                .projectId(projectId)
                .priority(priority)
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .contextName(contextName)
                .searchTerm(search)
                .build();
        
        List<Task> tasks = taskService.findByUserWithFilters(user.getId(), filter);
        List<TaskDto.TaskListResponse> response = tasks.stream()
                .map(taskService::toListResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskDto.TaskResponse> getTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Task task = taskService.findByIdAndUser(id, user.getId());
        return ResponseEntity.ok(taskService.toResponse(task));
    }
    
    @PostMapping("/one-time")
    @Operation(summary = "Create a one-time task")
    public ResponseEntity<TaskDto.TaskResponse> createOneTimeTask(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TaskDto.CreateOneTimeTaskRequest request) {
        Task task = taskService.createOneTimeTask(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.toResponse(task));
    }
    
    @PostMapping("/recurring")
    @Operation(summary = "Create a recurring task")
    public ResponseEntity<TaskDto.TaskResponse> createRecurringTask(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TaskDto.CreateRecurringTaskRequest request) {
        Task task = taskService.createRecurringTask(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.toResponse(task));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskDto.TaskResponse> updateTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody TaskDto.UpdateTaskRequest request) {
        Task task = taskService.update(id, user.getId(), request);
        return ResponseEntity.ok(taskService.toResponse(task));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        taskService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/complete")
    @Operation(summary = "Mark task as completed")
    public ResponseEntity<TaskDto.TaskResponse> completeTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Task task = taskService.complete(id, user.getId());
        return ResponseEntity.ok(taskService.toResponse(task));
    }
    
    @PostMapping("/{id}/defer")
    @Operation(summary = "Defer task to a new date")
    public ResponseEntity<TaskDto.TaskResponse> deferTask(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
        Task task = taskService.defer(id, user.getId(), newDate);
        return ResponseEntity.ok(taskService.toResponse(task));
    }
    
    @GetMapping("/overdue")
    @Operation(summary = "Get overdue tasks")
    public ResponseEntity<List<TaskDto.TaskListResponse>> getOverdueTasks(
            @AuthenticationPrincipal User user) {
        List<Task> tasks = taskService.findOverdue(user.getId());
        List<TaskDto.TaskListResponse> response = tasks.stream()
                .map(taskService::toListResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/today")
    @Operation(summary = "Get tasks for today")
    public ResponseEntity<List<TaskDto.TaskListResponse>> getTodayTasks(
            @AuthenticationPrincipal User user) {
        List<Task> tasks = taskService.findByDate(user.getId(), LocalDate.now());
        List<TaskDto.TaskListResponse> response = tasks.stream()
                .map(taskService::toListResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    // Recurring task instance endpoints
    @GetMapping("/instances")
    @Operation(summary = "Get task instances for date range")
    public ResponseEntity<List<TaskDto.TaskInstanceResponse>> getTaskInstances(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<TaskInstance> instances = taskService.findInstancesByDateRange(user.getId(), startDate, endDate);
        List<TaskDto.TaskInstanceResponse> response = instances.stream()
                .map(taskService::toInstanceResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/instances/{id}/complete")
    @Operation(summary = "Complete a specific task instance")
    public ResponseEntity<TaskDto.TaskInstanceResponse> completeTaskInstance(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        TaskInstance instance = taskService.completeInstance(id, user.getId());
        return ResponseEntity.ok(taskService.toInstanceResponse(instance));
    }
    
    @PostMapping("/instances/{id}/skip")
    @Operation(summary = "Skip a specific task instance")
    public ResponseEntity<TaskDto.TaskInstanceResponse> skipTaskInstance(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        TaskInstance instance = taskService.skipInstance(id, user.getId());
        return ResponseEntity.ok(taskService.toInstanceResponse(instance));
    }
    
    // Comments
    @PostMapping("/{id}/comments")
    @Operation(summary = "Add a comment to a task")
    public ResponseEntity<TaskDto.CommentResponse> addComment(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody TaskDto.CommentRequest request) {
        var comment = taskService.addComment(id, user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.toCommentResponse(comment));
    }
    
    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for a task")
    public ResponseEntity<List<TaskDto.CommentResponse>> getComments(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        var comments = taskService.getComments(id, user.getId());
        List<TaskDto.CommentResponse> response = comments.stream()
                .map(taskService::toCommentResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    // Contexts
    @PostMapping("/{id}/contexts/{contextId}")
    @Operation(summary = "Add context to task")
    public ResponseEntity<TaskDto.TaskResponse> addContext(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long contextId) {
        Task task = taskService.addContext(id, user.getId(), contextId);
        return ResponseEntity.ok(taskService.toResponse(task));
    }
    
    @DeleteMapping("/{id}/contexts/{contextId}")
    @Operation(summary = "Remove context from task")
    public ResponseEntity<TaskDto.TaskResponse> removeContext(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @PathVariable Long contextId) {
        Task task = taskService.removeContext(id, user.getId(), contextId);
        return ResponseEntity.ok(taskService.toResponse(task));
    }
}
