package com.taskmanager.dto.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {

    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private boolean archived;
    private double completionPercentage;
    private int totalTasks;
    private int completedTasks;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
