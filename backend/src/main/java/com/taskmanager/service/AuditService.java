package com.taskmanager.service;

import com.taskmanager.entity.AuditLog;
import com.taskmanager.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    
    private final AuditLogRepository auditLogRepository;
    
    @Transactional
    public void logAction(Long userId, String action, String entityType, Long entityId, String description) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();
        
        auditLogRepository.save(log);
    }
    
    @Transactional
    public void logActionWithChanges(Long userId, String action, String entityType, Long entityId, 
                                      String description, String changes) {
        AuditLog log = AuditLog.builder()
                .userId(userId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .changes(changes)
                .build();
        
        auditLogRepository.save(log);
    }
    
    public List<AuditLog> getAuditHistory(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
    
    public List<AuditLog> getEntityHistory(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }
    
    public List<AuditLog> getAuditHistoryForPeriod(Long userId, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByUserIdAndTimestampBetweenOrderByTimestampDesc(userId, start, end);
    }
}
