package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    @Builder.Default
    private String colorCode = "#3B82F6"; // Default blue
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean archived = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public double calculateCompletionPercentage() {
        if (tasks == null || tasks.isEmpty()) {
            return 0.0;
        }
        long completedCount = tasks.stream()
                .filter(task -> task.getStatus() == com.taskmanager.enums.TaskStatus.COMPLETED)
                .count();
        return (double) completedCount / tasks.size() * 100;
    }
    
    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }
    
    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
    
    public void archive() {
        this.archived = true;
    }
    
    public void unarchive() {
        this.archived = false;
    }
}
