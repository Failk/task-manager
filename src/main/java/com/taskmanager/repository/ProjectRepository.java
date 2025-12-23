package com.taskmanager.repository;

import com.taskmanager.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId AND p.archived = false ORDER BY p.createdAt DESC")
    List<Project> findByUserIdAndArchivedFalseOrderByCreatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId AND p.archived = true ORDER BY p.updatedAt DESC")
    List<Project> findByUserIdAndArchivedTrueOrderByUpdatedAtDesc(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    List<Project> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    Optional<Project> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id AND p.user.id = :userId")
    Optional<Project> findByIdAndUserIdWithTasks(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);
}
