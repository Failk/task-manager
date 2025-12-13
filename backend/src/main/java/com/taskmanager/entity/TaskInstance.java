package com.taskmanager.entity;

import com.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "task_instances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskInstance {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_task_id", nullable = false)
    private RecurringTask recurringTask;
    
    @Column(nullable = false)
    private LocalDate instanceDate;
    
    private LocalTime instanceTime;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean overridden = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean skipped = false;
    
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;
    
    private LocalDateTime completedAt;
    
    // Override fields (only used when overridden = true)
    private String overriddenTitle;
    private String overriddenDescription;
    private LocalDateTime overriddenDueDate;
    
    public LocalDateTime getEffectiveDueDate() {
        if (overridden && overriddenDueDate != null) {
            return overriddenDueDate;
        }
        return LocalDateTime.of(instanceDate, instanceTime != null ? instanceTime : LocalTime.MIDNIGHT);
    }
    
    public String getEffectiveTitle() {
        if (overridden && overriddenTitle != null) {
            return overriddenTitle;
        }
        return recurringTask != null ? recurringTask.getTitle() : "";
    }
    
    public String getEffectiveDescription() {
        if (overridden && overriddenDescription != null) {
            return overriddenDescription;
        }
        return recurringTask != null ? recurringTask.getDescription() : "";
    }
    
    public void markComplete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }

    public void skip() {
        this.skipped = true;
        this.status = TaskStatus.DEFERRED;
    }

    public void overrideForInstance(String title, String description, LocalDateTime dueDate) {
        this.overridden = true;
        this.overriddenTitle = title;
        this.overriddenDescription = description;
        this.overriddenDueDate = dueDate;
    }

    public LocalDate getScheduledDate() {
        return instanceDate;
    }

    public String getOverrideTitle() {
        return overriddenTitle;
    }

    public String getOverrideDescription() {
        return overriddenDescription;
    }
}
