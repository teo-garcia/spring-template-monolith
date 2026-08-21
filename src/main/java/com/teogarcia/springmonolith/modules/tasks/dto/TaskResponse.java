package com.teogarcia.springmonolith.modules.tasks.dto;

import java.time.Instant;

import com.teogarcia.springmonolith.modules.tasks.Task;
import com.teogarcia.springmonolith.modules.tasks.TaskStatus;

public record TaskResponse(
    String id,
    String title,
    String description,
    TaskStatus status,
    int priority,
    Instant createdAt,
    Instant updatedAt) {

  public static TaskResponse from(Task t) {
    return new TaskResponse(
        t.getId(),
        t.getTitle(),
        t.getDescription(),
        t.getStatus(),
        t.getPriority(),
        t.getCreatedAt(),
        t.getUpdatedAt());
  }
}
