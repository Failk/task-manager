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
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Project> findByUserIdAndArchivedOrderByCreatedAtDesc(Long userId, boolean archived);
    Optional<Project> findByIdAndUserId(Long id, Long userId);
    
    @Query("SELECT p FROM Project p LEFT JOIN FETCH p.tasks WHERE p.id = :id AND p.user.id = :userId")
    Optional<Project> findByIdAndUserIdWithTasks(@Param("id") Long id, @Param("userId") Long userId);
    
    boolean existsByNameAndUserId(String name, Long userId);
}
