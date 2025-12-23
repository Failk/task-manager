package com.taskmanager.controller;

import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.dto.project.CreateProjectRequest;
import com.taskmanager.dto.project.ProjectDTO;
import com.taskmanager.dto.project.UpdateProjectRequest;
import com.taskmanager.service.ProjectService;
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
@RequestMapping("/projects")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Project Management", description = "Project CRUD operations")
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Get all user projects")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getAllProjects() {
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @Operation(summary = "Get archived projects")
    @GetMapping("/archived")
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getArchivedProjects() {
        List<ProjectDTO> projects = projectService.getArchivedProjects();
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    @Operation(summary = "Get project by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getProjectById(@PathVariable Long id) {
        ProjectDTO project = projectService.getProjectById(id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    @Operation(summary = "Create new project")
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @Valid @RequestBody CreateProjectRequest request) {
        ProjectDTO project = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    @Operation(summary = "Update project")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {
        ProjectDTO project = projectService.updateProject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    @Operation(summary = "Delete project")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully", null));
    }

    @Operation(summary = "Archive project")
    @PostMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProjectDTO>> archiveProject(@PathVariable Long id) {
        ProjectDTO project = projectService.archiveProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project archived successfully", project));
    }

    @Operation(summary = "Unarchive project")
    @PostMapping("/{id}/unarchive")
    public ResponseEntity<ApiResponse<ProjectDTO>> unarchiveProject(@PathVariable Long id) {
        ProjectDTO project = projectService.unarchiveProject(id);
        return ResponseEntity.ok(ApiResponse.success("Project unarchived successfully", project));
    }
}
