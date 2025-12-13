package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private String action; // CREATE, UPDATE, DELETE, COMPLETE, etc.
    
    @Column(nullable = false)
    private String entityType; // TASK, PROJECT, USER, etc.
    
    @Column(nullable = false)
    private Long entityId;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column(length = 4000)
    private String changes; // JSON representation of changes
    
    @Column(length = 500)
    private String description;
    
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
    
    public static AuditLog create(Long userId, String action, String entityType, Long entityId, String changes) {
        return AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .changes(changes)
                .build();
    }
}
