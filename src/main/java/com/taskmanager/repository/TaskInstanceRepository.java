package com.taskmanager.repository;

import com.taskmanager.entity.TaskInstance;
import com.taskmanager.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskInstanceRepository extends JpaRepository<TaskInstance, Long> {

    List<TaskInstance> findByRecurringTaskId(Long recurringTaskId);

    @Query("SELECT ti FROM TaskInstance ti JOIN FETCH ti.recurringTask rt LEFT JOIN FETCH rt.project WHERE rt.user.id = :userId AND ti.scheduledDate = :date ORDER BY ti.scheduledTime ASC")
    List<TaskInstance> findByUserIdAndScheduledDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT ti FROM TaskInstance ti JOIN FETCH ti.recurringTask rt LEFT JOIN FETCH rt.project WHERE rt.user.id = :userId AND ti.scheduledDate BETWEEN :startDate AND :endDate ORDER BY ti.scheduledDate ASC")
    List<TaskInstance> findByUserIdAndScheduledDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT ti FROM TaskInstance ti WHERE ti.recurringTask.id = :taskId AND ti.scheduledDate >= :fromDate ORDER BY ti.scheduledDate ASC")
    List<TaskInstance> findFutureInstancesByTaskId(@Param("taskId") Long taskId, @Param("fromDate") LocalDate fromDate);

    Optional<TaskInstance> findByRecurringTaskIdAndScheduledDate(Long recurringTaskId, LocalDate scheduledDate);

    @Query("SELECT ti FROM TaskInstance ti WHERE ti.recurringTask.user.id = :userId AND ti.status = :status")
    List<TaskInstance> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
}
