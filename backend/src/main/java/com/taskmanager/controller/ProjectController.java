package com.taskmanager.controller;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    
    private final ProjectService projectService;
    
    @GetMapping
    @Operation(summary = "Get all projects for current user")
    public ResponseEntity<List<ProjectDto.ProjectListResponse>> getAllProjects(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "false") boolean includeArchived) {
        List<Project> projects = projectService.findByUser(user.getId(), includeArchived);
        List<ProjectDto.ProjectListResponse> response = projects.stream()
                .map(projectService::toListResponse)
                .toList();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectDto.ProjectResponse> getProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Project project = projectService.findByIdAndUser(id, user.getId());
        return ResponseEntity.ok(projectService.toResponse(project));
    }
    
    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectDto.ProjectResponse> createProject(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProjectDto.CreateProjectRequest request) {
        Project project = projectService.create(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.toResponse(project));
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectDto.ProjectResponse> updateProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody ProjectDto.UpdateProjectRequest request) {
        Project project = projectService.update(id, user.getId(), request);
        return ResponseEntity.ok(projectService.toResponse(project));
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        projectService.delete(id, user.getId());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive a project")
    public ResponseEntity<ProjectDto.ProjectResponse> archiveProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Project project = projectService.archive(id, user.getId());
        return ResponseEntity.ok(projectService.toResponse(project));
    }
    
    @PostMapping("/{id}/unarchive")
    @Operation(summary = "Unarchive a project")
    public ResponseEntity<ProjectDto.ProjectResponse> unarchiveProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        Project project = projectService.unarchive(id, user.getId());
        return ResponseEntity.ok(projectService.toResponse(project));
    }
}
