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
    
    Optional<Context> findByName(String name);
    
    @Query("SELECT c FROM Context c WHERE c.user IS NULL OR c.user.id = :userId")
    List<Context> findAllAvailableForUser(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Context c WHERE c.user.id = :userId")
    List<Context> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT c FROM Context c WHERE c.user IS NULL")
    List<Context> findDefaultContexts();
    
    boolean existsByNameAndUserId(String name, Long userId);
}
