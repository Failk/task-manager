package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(nullable = false)
    @Builder.Default
    private boolean archived = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public double getCompletionPercentage() {
        int totalCount = getTotalTasks();
        if (totalCount == 0) {
            return 0.0;
        }
        return (double) getCompletedTasks() / totalCount * 100;
    }

    public int getTotalTasks() {
        int count = 0;
        for (Task task : tasks) {
            if (task instanceof RecurringTask) {
                RecurringTask rt = (RecurringTask) task;
                // Count all instances for recurring tasks
                int instanceCount = rt.getTaskInstances().size();
                count += instanceCount > 0 ? instanceCount : 1; // At least 1 if no instances yet
            } else {
                // Count one-time tasks as 1
                count += 1;
            }
        }
        return count;
    }

    public int getCompletedTasks() {
        int count = 0;
        for (Task task : tasks) {
            if (task instanceof RecurringTask) {
                RecurringTask rt = (RecurringTask) task;
                // Count completed instances for recurring tasks
                count += (int) rt.getTaskInstances().stream()
                        .filter(ti -> ti.getStatus() == com.taskmanager.entity.enums.TaskStatus.COMPLETED)
                        .count();
            } else {
                // Count completed one-time tasks
                if (task.getStatus() == com.taskmanager.entity.enums.TaskStatus.COMPLETED) {
                    count += 1;
                }
            }
        }
        return count;
    }
}
