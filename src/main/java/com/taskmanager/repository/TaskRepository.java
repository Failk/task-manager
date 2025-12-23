package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId ORDER BY t.dueDate ASC, t.priority ASC")
    List<Task> findByUserIdOrderByDueDateAscPriorityAsc(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.id = :id AND t.user.id = :userId")
    Optional<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND t.status = :status")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND t.priority = :priority")
    List<Task> findByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Priority priority);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.contexts WHERE t.project.id = :projectId")
    List<Task> findByProjectId(@Param("projectId") Long projectId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND t.dueDate = :date ORDER BY t.priority ASC")
    List<Task> findByUserIdAndDueDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND t.dueDate < :date AND t.status NOT IN ('COMPLETED', 'CANCELLED') ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND t.dueDate BETWEEN :startDate AND :endDate ORDER BY t.dueDate ASC, t.priority ASC")
    List<Task> findByUserIdAndDueDateBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project JOIN FETCH t.contexts c WHERE t.user.id = :userId AND c.id = :contextId ORDER BY t.dueDate ASC")
    List<Task> findByUserIdAndContextId(@Param("userId") Long userId, @Param("contextId") Long contextId);

    @Query("SELECT DISTINCT t FROM Task t LEFT JOIN FETCH t.project LEFT JOIN FETCH t.contexts WHERE t.user.id = :userId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> searchByKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status = 'COMPLETED'")
    long countCompletedTasks(@Param("userId") Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    long countPendingTasks(@Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
