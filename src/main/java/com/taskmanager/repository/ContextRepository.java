package com.taskmanager.repository;

import com.taskmanager.entity.Context;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContextRepository extends JpaRepository<Context, Long> {
    
    List<Context> findByIsDefaultTrue();
    
    @Query("SELECT c FROM Context c WHERE c.isDefault = true OR c.user.id = :userId ORDER BY c.isDefault DESC, c.name ASC")
    List<Context> findByUserIdOrDefault(@Param("userId") Long userId);
    
    List<Context> findByUserId(Long userId);
    
    Optional<Context> findByName(String name);
    
    Optional<Context> findByIdAndUserId(Long id, Long userId);
    
    boolean existsByNameAndUserId(String name, Long userId);
}
