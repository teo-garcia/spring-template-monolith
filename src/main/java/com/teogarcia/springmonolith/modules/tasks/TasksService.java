package com.teogarcia.springmonolith.modules.tasks;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.teogarcia.springmonolith.modules.tasks.dto.CreateTaskRequest;
import com.teogarcia.springmonolith.modules.tasks.dto.PaginatedTasksResponse;
import com.teogarcia.springmonolith.modules.tasks.dto.TaskResponse;
import com.teogarcia.springmonolith.modules.tasks.dto.UpdateTaskRequest;
import com.teogarcia.springmonolith.shared.exception.ResourceNotFoundException;

@Service
public class TasksService {

  private static final Logger log = LoggerFactory.getLogger(TasksService.class);
  private final TaskRepository repository;

  public TasksService(TaskRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public TaskResponse create(CreateTaskRequest req) {
    String id = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    Task task =
        new Task(
            id,
            req.title(),
            req.description(),
            req.status() != null ? req.status() : TaskStatus.PENDING,
            req.priority() != null ? req.priority() : 0);
    task = repository.saveAndFlush(task);
    log.info("Created task {}", task.getId());
    return TaskResponse.from(task);
  }

  @Transactional(readOnly = true)
  public PaginatedTasksResponse findAll(
      TaskStatus status, Integer priority, int page, int pageSize) {
    Page<Task> result =
        repository.findFiltered(
            status,
            priority,
            PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    return new PaginatedTasksResponse(
        result.getContent().stream().map(TaskResponse::from).toList(),
        new PaginatedTasksResponse.PaginationMeta(result.getTotalElements(), page, pageSize));
  }

  @Transactional(readOnly = true)
  public TaskResponse findOne(String id) {
    Task task =
        repository
            .findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    return TaskResponse.from(task);
  }

  @Transactional
  public TaskResponse update(String id, UpdateTaskRequest req) {
    Task task =
        repository
            .findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    if (req.title() != null) task.setTitle(req.title());
    if (req.description() != null) task.setDescription(req.description());
    if (req.status() != null) task.setStatus(req.status());
    if (req.priority() != null) task.setPriority(req.priority());
    task = repository.saveAndFlush(task);
    log.info("Updated task {}", id);
    return TaskResponse.from(task);
  }

  @Transactional
  public void delete(String id) {
    Task task =
        repository
            .findActiveById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    // Soft delete
    task.setDeletedAt(Instant.now());
    repository.save(task);
    log.info("Soft-deleted task {}", id);
  }
}
