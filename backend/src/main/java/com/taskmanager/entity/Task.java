package com.taskmanager.entity;

import com.taskmanager.enums.Priority;
import com.taskmanager.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "task_type", discriminatorType = DiscriminatorType.STRING)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(exclude = {"contexts", "reminders", "comments"})
public abstract class Task {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.C;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskStatus status = TaskStatus.NOT_STARTED;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime completedAt;
    
    private Integer estimatedDurationMinutes;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;
    
    @ManyToMany
    @JoinTable(
        name = "task_contexts",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "context_id")
    )
    @Builder.Default
    private Set<Context> contexts = new HashSet<>();
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reminder> reminders = new ArrayList<>();
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskComment> comments = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void markComplete() {
        this.status = TaskStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();
    }
    
    public void updateStatus(TaskStatus newStatus) {
        this.status = newStatus;
        if (newStatus == TaskStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }
    
    public void addComment(TaskComment comment) {
        comments.add(comment);
        comment.setTask(this);
    }
    
    public void addReminder(Reminder reminder) {
        reminders.add(reminder);
        reminder.setTask(this);
    }
    
    public void addContext(Context context) {
        contexts.add(context);
    }
    
    public void removeContext(Context context) {
        contexts.remove(context);
    }
    
    public boolean isOverdue() {
        return status != TaskStatus.COMPLETED && 
               dueDate != null && 
               dueDate.isBefore(LocalDateTime.now());
    }
    
    public abstract String getTaskType();
}
