package com.taskmanager.repository;

import com.taskmanager.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<AuditLog> findByEntityTypeAndEntityIdOrderByTimestampDesc(String entityType, Long entityId);
    
    List<AuditLog> findByUserIdAndTimestampBetweenOrderByTimestampDesc(
            Long userId, LocalDateTime start, LocalDateTime end);
}
