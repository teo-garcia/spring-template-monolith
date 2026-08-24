package com.teogarcia.springmonolith.shared.openapi;

import java.time.Instant;

import com.teogarcia.springmonolith.modules.tasks.dto.PaginatedTasksResponse;
import com.teogarcia.springmonolith.modules.tasks.dto.TaskResponse;

import io.swagger.v3.oas.annotations.media.Schema;

/** OpenAPI-only types describing the success envelopes emitted by TransformInterceptor. */
public final class ApiEnvelopeSchemas {

  private ApiEnvelopeSchemas() {}

  @Schema(name = "SuccessMeta")
  public record SuccessMeta(String requestId, String version) {}

  @Schema(name = "TaskSuccessEnvelope")
  public record TaskSuccessEnvelope(
      boolean success,
      int statusCode,
      Instant timestamp,
      String path,
      String method,
      TaskResponse data,
      SuccessMeta meta) {}

  @Schema(name = "PaginatedTasksSuccessEnvelope")
  public record PaginatedTasksSuccessEnvelope(
      boolean success,
      int statusCode,
      Instant timestamp,
      String path,
      String method,
      PaginatedTasksResponse data,
      SuccessMeta meta) {}
}
