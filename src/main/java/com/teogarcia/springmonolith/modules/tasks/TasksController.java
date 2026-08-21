package com.teogarcia.springmonolith.modules.tasks;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.teogarcia.springmonolith.modules.tasks.dto.CreateTaskRequest;
import com.teogarcia.springmonolith.modules.tasks.dto.PaginatedTasksResponse;
import com.teogarcia.springmonolith.modules.tasks.dto.TaskResponse;
import com.teogarcia.springmonolith.modules.tasks.dto.UpdateTaskRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("${app.api-prefix:/api/v1}/tasks")
@Tag(name = "Tasks")
public class TasksController {

  private final TasksService service;

  public TasksController(TasksService service) {
    this.service = service;
  }

  @GetMapping
  @Operation(summary = "List tasks with pagination")
  public PaginatedTasksResponse list(
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Integer priority,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "20") int pageSize) {
    if (page < 1) page = 1;
    if (pageSize < 1) pageSize = 20;
    if (pageSize > 100) pageSize = 100;
    return service.findAll(status, priority, page, pageSize);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get task by id")
  public TaskResponse getOne(@PathVariable String id) {
    return service.findOne(id);
  }

  @PostMapping
  @Operation(summary = "Create task")
  public ResponseEntity<TaskResponse> create(@Valid @RequestBody CreateTaskRequest req) {
    TaskResponse created = service.create(req);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PatchMapping("/{id}")
  @Operation(summary = "Update task")
  public TaskResponse update(@PathVariable String id, @Valid @RequestBody UpdateTaskRequest req) {
    return service.update(id, req);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete task (soft delete)")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
