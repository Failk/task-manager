package com.taskmanager.repository;

import com.taskmanager.entity.Reminder;
import com.taskmanager.enums.ReminderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    List<Reminder> findByTaskId(Long taskId);
    
    @Query("SELECT r FROM Reminder r WHERE r.sent = false AND r.acknowledged = false " +
           "AND r.task.status != 'COMPLETED' " +
           "AND (r.snoozedUntil IS NULL OR r.snoozedUntil <= :now)")
    List<Reminder> findPendingReminders(@Param("now") LocalDateTime now);

    @Query("SELECT r FROM Reminder r WHERE r.task.project.user.id = :userId " +
           "AND r.sent = false AND r.acknowledged = false " +
           "AND r.task.status != 'COMPLETED' " +
           "AND (r.snoozedUntil IS NULL OR r.snoozedUntil <= :now)")
    List<Reminder> findPendingReminders(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reminder r WHERE r.reminderType = :type AND r.sent = false " +
           "AND r.task.status != 'COMPLETED' " +
           "AND r.task.dueDate BETWEEN :start AND :end")
    List<Reminder> findByTypeAndDueDateBetween(
            @Param("type") ReminderType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT r FROM Reminder r WHERE r.task.project.user.id = :userId ORDER BY r.sentAt DESC")
    List<Reminder> findNotificationHistory(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reminder r WHERE r.task.project.user.id = :userId AND r.sent = true ORDER BY r.sentAt DESC")
    List<Reminder> findSentRemindersByUserId(@Param("userId") Long userId);
}
