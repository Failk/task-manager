package com.taskmanager.service;

import com.taskmanager.dto.project.CreateProjectRequest;
import com.taskmanager.dto.project.ProjectDTO;
import com.taskmanager.dto.project.UpdateProjectRequest;
import com.taskmanager.entity.Project;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserService userService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects() {
        User user = userService.getCurrentUser();
        return projectRepository.findByUserIdAndArchivedFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getArchivedProjects() {
        User user = userService.getCurrentUser();
        return projectRepository.findByUserIdAndArchivedTrueOrderByUpdatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long id) {
        User user = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndUserIdWithTasks(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));
        return mapToDTO(project);
    }

    @Transactional
    public ProjectDTO createProject(CreateProjectRequest request) {
        User user = userService.getCurrentUser();

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .user(user)
                .build();

        project = projectRepository.save(project);
        log.info("Project created: {} by user: {}", project.getName(), user.getEmail());

        auditService.logAction("Project", project.getId(), "CREATE", null, project.toString());

        return mapToDTO(project);
    }

    @Transactional
    public ProjectDTO updateProject(Long id, UpdateProjectRequest request) {
        User user = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        String oldValues = project.toString();

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getDueDate() != null) {
            project.setDueDate(request.getDueDate());
        }

        project = projectRepository.save(project);
        log.info("Project updated: {} by user: {}", project.getName(), user.getEmail());

        auditService.logAction("Project", project.getId(), "UPDATE", oldValues, project.toString());

        return mapToDTO(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        User user = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        auditService.logAction("Project", project.getId(), "DELETE", project.toString(), null);

        projectRepository.delete(project);
        log.info("Project deleted: {} by user: {}", project.getName(), user.getEmail());
    }

    @Transactional
    public ProjectDTO archiveProject(Long id) {
        User user = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setArchived(true);
        project = projectRepository.save(project);
        log.info("Project archived: {} by user: {}", project.getName(), user.getEmail());

        auditService.logAction("Project", project.getId(), "ARCHIVE", "archived=false", "archived=true");

        return mapToDTO(project);
    }

    @Transactional
    public ProjectDTO unarchiveProject(Long id) {
        User user = userService.getCurrentUser();
        Project project = projectRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", id));

        project.setArchived(false);
        project = projectRepository.save(project);
        log.info("Project unarchived: {} by user: {}", project.getName(), user.getEmail());

        auditService.logAction("Project", project.getId(), "UNARCHIVE", "archived=true", "archived=false");

        return mapToDTO(project);
    }

    private ProjectDTO mapToDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .dueDate(project.getDueDate())
                .archived(project.isArchived())
                .completionPercentage(project.getCompletionPercentage())
                .totalTasks(project.getTotalTasks())
                .completedTasks(project.getCompletedTasks())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
