package com.taskmanager.service;

import com.taskmanager.dto.ProjectDto;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.enums.TaskStatus;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    private final AuditService auditService;
    
    @Transactional
    public Project create(Long userId, ProjectDto.CreateRequest request, User user) {
        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .colorCode(request.getColorCode() != null ? request.getColorCode() : "#3B82F6")
                .user(user)
                .archived(false)
                .build();
        
        Project saved = projectRepository.save(project);
        auditService.logAction(userId, "CREATE", "PROJECT", saved.getId(), "Created project: " + saved.getName());
        
        return saved;
    }
    
    public List<Project> findAllByUserId(Long userId) {
        return projectRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Project> findActiveByUserId(Long userId) {
        return projectRepository.findByUserIdAndArchivedOrderByCreatedAtDesc(userId, false);
    }
    
    public Project findByIdAndUserId(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserId(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
    
    public Project findByIdAndUserIdWithTasks(Long projectId, Long userId) {
        return projectRepository.findByIdAndUserIdWithTasks(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
    
    @Transactional
    public Project update(Long projectId, Long userId, ProjectDto.UpdateRequest request) {
        Project project = findByIdAndUserId(projectId, userId);
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getColorCode() != null) {
            project.setColorCode(request.getColorCode());
        }
        
        Project saved = projectRepository.save(project);
        auditService.logAction(userId, "UPDATE", "PROJECT", saved.getId(), "Updated project: " + saved.getName());
        
        return saved;
    }
    
    @Transactional
    public void delete(Long projectId, Long userId) {
        Project project = findByIdAndUserId(projectId, userId);
        String projectName = project.getName();
        projectRepository.delete(project);
        auditService.logAction(userId, "DELETE", "PROJECT", projectId, "Deleted project: " + projectName);
    }
    
    @Transactional
    public Project archive(Long projectId, Long userId) {
        Project project = findByIdAndUserId(projectId, userId);
        project.archive();
        Project saved = projectRepository.save(project);
        auditService.logAction(userId, "ARCHIVE", "PROJECT", saved.getId(), "Archived project: " + saved.getName());
        return saved;
    }
    
    @Transactional
    public Project unarchive(Long projectId, Long userId) {
        Project project = findByIdAndUserId(projectId, userId);
        project.unarchive();
        Project saved = projectRepository.save(project);
        auditService.logAction(userId, "UNARCHIVE", "PROJECT", saved.getId(), "Unarchived project: " + saved.getName());
        return saved;
    }
    
    public ProjectDto.ProjectResponse toResponse(Project project) {
        int taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        int completedCount = project.getTasks() != null ? 
                (int) project.getTasks().stream()
                        .filter(t -> t.getStatus() == TaskStatus.COMPLETED)
                        .count() : 0;
        
        return ProjectDto.ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .colorCode(project.getColorCode())
                .createdAt(project.getCreatedAt())
                .archived(project.isArchived())
                .taskCount(taskCount)
                .completedTaskCount(completedCount)
                .completionPercentage(project.calculateCompletionPercentage())
                .build();
    }
    
    public ProjectDto.ProjectListResponse toListResponse(Project project) {
        int taskCount = project.getTasks() != null ? project.getTasks().size() : 0;
        
        return ProjectDto.ProjectListResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .colorCode(project.getColorCode())
                .archived(project.isArchived())
                .taskCount(taskCount)
                .completionPercentage(project.calculateCompletionPercentage())
                .build();
    }
    
    public List<ProjectDto.ProjectListResponse> toListResponse(List<Project> projects) {
        return projects.stream()
                .map(this::toListResponse)
                .collect(Collectors.toList());
    }
}
