package com.taskmanager.service;

import com.taskmanager.entity.AuditLog;
import com.taskmanager.entity.User;
import com.taskmanager.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Async
    public void logAction(String entityType, Long entityId, String action, String oldValues, String newValues) {
        try {
            Long userId = getCurrentUserId();
            String userEmail = getCurrentUserEmail();

            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .oldValues(oldValues)
                    .newValues(newValues)
                    .userId(userId)
                    .userEmail(userEmail)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log created: {} {} on {} {}", action, entityType, entityId, userEmail);
        } catch (Exception e) {
            log.error("Failed to create audit log: {}", e.getMessage());
        }
    }

    public List<AuditLog> getAuditLogsForEntity(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
    }

    public List<AuditLog> getAuditLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end);
    }

    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        } catch (Exception e) {
            log.warn("Could not get current user ID for audit log");
        }
        return null;
    }

    private String getCurrentUserEmail() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            log.warn("Could not get current user email for audit log");
        }
        return "unknown";
    }
}
