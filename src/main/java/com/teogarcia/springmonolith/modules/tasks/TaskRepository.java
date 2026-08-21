package com.teogarcia.springmonolith.modules.tasks;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, String> {

  @Query(
      """
      SELECT t FROM Task t WHERE t.deletedAt IS NULL
        AND (:status IS NULL OR t.status = :status)
        AND (:minPriority IS NULL OR t.priority >= :minPriority)
      """)
  Page<Task> findFiltered(
      @Param("status") TaskStatus status,
      @Param("minPriority") Integer minPriority,
      Pageable pageable);

  @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deletedAt IS NULL")
  Optional<Task> findActiveById(@Param("id") String id);
}
