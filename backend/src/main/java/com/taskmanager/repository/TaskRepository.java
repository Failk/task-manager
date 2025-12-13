package com.taskmanager.repository;

import com.taskmanager.entity.Task;
import com.taskmanager.enums.Priority;
import com.taskmanager.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId ORDER BY t.dueDate ASC")
    List<Task> findAllByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND t.project.id = :projectId ORDER BY t.dueDate ASC")
    List<Task> findByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.project.user.id = :userId")
    Optional<Task> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND t.dueDate BETWEEN :start AND :end ORDER BY t.dueDate ASC")
    List<Task> findByUserIdAndDueDateBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND t.status != 'COMPLETED' AND t.dueDate < :now ORDER BY t.dueDate ASC")
    List<Task> findOverdueTasks(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND t.priority = :priority ORDER BY t.dueDate ASC")
    List<Task> findByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Priority priority);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND t.status = :status ORDER BY t.dueDate ASC")
    List<Task> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
    
    @Query("SELECT t FROM Task t WHERE t.project.user.id = :userId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY t.dueDate ASC")
    List<Task> searchByTitleOrDescription(@Param("userId") Long userId, @Param("query") String query);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.user.id = :userId AND t.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.project.user.id = :userId AND t.priority = :priority")
    long countByUserIdAndPriority(@Param("userId") Long userId, @Param("priority") Priority priority);
    
    @Query("SELECT t FROM Task t JOIN t.contexts c WHERE t.project.user.id = :userId AND c.name = :contextName ORDER BY t.dueDate ASC")
    List<Task> findByUserIdAndContextName(@Param("userId") Long userId, @Param("contextName") String contextName);
}
