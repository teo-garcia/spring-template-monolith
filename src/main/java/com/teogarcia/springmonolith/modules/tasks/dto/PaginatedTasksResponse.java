package com.teogarcia.springmonolith.modules.tasks.dto;

import java.util.List;

public record PaginatedTasksResponse(List<TaskResponse> data, PaginationMeta meta) {
  public record PaginationMeta(long total, int page, int pageSize) {}
}
