package com.taskmanager.controller;

import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.dto.task.*;
import com.taskmanager.entity.enums.TaskStatus;
import com.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Task Management", description = "Task CRUD operations")
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Get all tasks with optional filtering")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Get today's tasks")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTodayTasks() {
        List<TaskDTO> tasks = taskService.getTasksForToday();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Get overdue tasks")
    @GetMapping("/overdue")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getOverdueTasks() {
        List<TaskDTO> tasks = taskService.getOverdueTasks();
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Get task by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> getTaskById(@PathVariable Long id) {
        TaskDTO task = taskService.getTaskById(id);
        return ResponseEntity.ok(ApiResponse.success(task));
    }

    @Operation(summary = "Filter tasks")
    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> filterTasks(
            @RequestBody TaskFilterRequest filter) {
        List<TaskDTO> tasks = taskService.filterTasks(filter);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Create one-time task via TaskFactory")
    @PostMapping("/one-time")
    public ResponseEntity<ApiResponse<TaskDTO>> createOneTimeTask(
            @Valid @RequestBody CreateOneTimeTaskRequest request) {
        TaskDTO task = taskService.createOneTimeTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Task created successfully", task));
    }

    @Operation(summary = "Create recurring task via TaskFactory")
    @PostMapping("/recurring")
    public ResponseEntity<ApiResponse<TaskDTO>> createRecurringTask(
            @Valid @RequestBody CreateRecurringTaskRequest request) {
        TaskDTO task = taskService.createRecurringTask(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Recurring task created successfully", task));
    }

    @Operation(summary = "Update task")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request) {
        TaskDTO task = taskService.updateTask(id, request);
        return ResponseEntity.ok(ApiResponse.success("Task updated successfully", task));
    }

    @Operation(summary = "Mark task as complete")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<TaskDTO>> completeTask(@PathVariable Long id) {
        TaskDTO task = taskService.completeTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task marked as complete", task));
    }

    @Operation(summary = "Update task status")
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskDTO>> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam TaskStatus status) {
        TaskDTO task = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Task status updated", task));
    }

    @Operation(summary = "Delete task")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully", null));
    }

    @Operation(summary = "Get tasks by project")
    @GetMapping("/project/{projectId}")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasksByProject(@PathVariable Long projectId) {
        List<TaskDTO> tasks = taskService.getTasksByProject(projectId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Get tasks by context")
    @GetMapping("/context/{contextId}")
    public ResponseEntity<ApiResponse<List<TaskDTO>>> getTasksByContext(@PathVariable Long contextId) {
        List<TaskDTO> tasks = taskService.getTasksByContext(contextId);
        return ResponseEntity.ok(ApiResponse.success(tasks));
    }

    @Operation(summary = "Update task instance status (for recurring tasks)")
    @PutMapping("/instance/{instanceId}/status")
    public ResponseEntity<ApiResponse<Void>> updateInstanceStatus(
            @PathVariable Long instanceId,
            @RequestParam TaskStatus status) {
        taskService.updateTaskInstanceStatus(instanceId, status);
        return ResponseEntity.ok(ApiResponse.success("Task instance status updated", null));
    }
}
