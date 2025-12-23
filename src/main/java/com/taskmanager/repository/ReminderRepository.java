package com.taskmanager.repository;

import com.taskmanager.entity.Reminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    
    List<Reminder> findByTaskId(Long taskId);
    
    @Query("SELECT r FROM Reminder r WHERE r.sent = false AND r.remindAt <= :now AND (r.snoozedUntil IS NULL OR r.snoozedUntil <= :now)")
    List<Reminder> findPendingReminders(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reminder r WHERE r.task.user.id = :userId AND r.sent = false ORDER BY r.remindAt ASC")
    List<Reminder> findUpcomingRemindersByUserId(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Reminder r WHERE r.task.user.id = :userId AND r.acknowledged = false ORDER BY r.remindAt DESC")
    List<Reminder> findUnacknowledgedRemindersByUserId(@Param("userId") Long userId);
}
