package com.taskforge.repository;

import com.taskforge.entity.Task;
import com.taskforge.entity.Task.Priority;
import com.taskforge.entity.Task.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Paginated query for user tasks with optional filters
    @Query("""
        SELECT t FROM Task t
        WHERE t.user.id = :userId
          AND (:status   IS NULL OR t.status   = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:search   IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
          OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Task> findByUserIdWithFilters(
        @Param("userId")   Long userId,
        @Param("status")   Status status,
        @Param("priority") Priority priority,
        @Param("search")   String search,
        Pageable pageable
    );

    // Admin: all tasks with filters
    @Query("""
        SELECT t FROM Task t
        WHERE (:status   IS NULL OR t.status   = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:search   IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
                                 OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Task> findAllWithFilters(
        @Param("status")   Status status,
        @Param("priority") Priority priority,
        @Param("search")   String search,
        Pageable pageable
    );

    long countByUserIdAndStatus(Long userId, Status status);
    long countByUserId(Long userId);
}
