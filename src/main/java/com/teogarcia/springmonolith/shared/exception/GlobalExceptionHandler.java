package com.teogarcia.springmonolith.shared.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.teogarcia.springmonolith.shared.filter.RequestIdFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorEnvelope> handleValidation(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, Object> fieldErrors = new HashMap<>();
    ex.getBindingResult()
        .getFieldErrors()
        .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
    return build(
        request,
        HttpStatus.UNPROCESSABLE_CONTENT,
        "Validation failed",
        "ValidationError",
        fieldErrors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorEnvelope> handleConstraint(
      ConstraintViolationException ex, HttpServletRequest request) {
    return build(
        request, HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage(), "ValidationError", null);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorEnvelope> handleNotReadable(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return build(
        request, HttpStatus.BAD_REQUEST, "Malformed request body", "BadRequestError", null);
  }

  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<ErrorEnvelope> handleResponseStatus(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
    return build(request, status, reason, toErrorName(status), null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorEnvelope> handleConflict(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
    return build(
        request,
        HttpStatus.CONFLICT,
        "A record with this value already exists",
        "ConflictError",
        null);
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ErrorEnvelope> handleNotFound(
      ResourceNotFoundException ex, HttpServletRequest request) {
    return build(request, HttpStatus.NOT_FOUND, ex.getMessage(), "NotFoundError", null);
  }

  /**
   * An unmatched route raises NoResourceFoundException. Without this handler it falls through to
   * handleUnknown and every 404 is reported as a 500.
   */
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorEnvelope> handleNoResource(
      NoResourceFoundException ex, HttpServletRequest request) {
    return build(request, HttpStatus.NOT_FOUND, "Resource not found", "NotFoundError", null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorEnvelope> handleUnknown(Exception ex, HttpServletRequest request) {
    String requestId = (String) request.getAttribute(RequestIdFilter.ATTR);
    log.error(
        "Unhandled error [{}] {} {}", requestId, request.getMethod(), request.getRequestURI(), ex);
    return build(
        request,
        HttpStatus.INTERNAL_SERVER_ERROR,
        "Internal server error",
        "InternalServerError",
        null);
  }

  private ResponseEntity<ErrorEnvelope> build(
      HttpServletRequest request,
      HttpStatus status,
      String message,
      String error,
      Map<String, Object> errors) {
    String requestId = (String) request.getAttribute(RequestIdFilter.ATTR);
    Map<String, String> meta = requestId != null ? Map.of("requestId", requestId) : null;
    String query = request.getQueryString();
    String path = query != null ? request.getRequestURI() + "?" + query : request.getRequestURI();
    ErrorEnvelope body =
        new ErrorEnvelope(
            false,
            status.value(),
            Instant.now().toString(),
            path,
            request.getMethod(),
            message,
            error,
            errors,
            meta);
    if (status.is5xxServerError()) {
      log.error("{} {} {} - {}", request.getMethod(), path, status.value(), message);
    } else {
      log.debug("{} {} {} - {}", request.getMethod(), path, status.value(), message);
    }
    return ResponseEntity.status(status).body(body);
  }

  private String toErrorName(HttpStatus status) {
    return switch (status) {
      case BAD_REQUEST -> "BadRequestError";
      case UNAUTHORIZED -> "UnauthorizedError";
      case FORBIDDEN -> "ForbiddenError";
      case NOT_FOUND -> "NotFoundError";
      case CONFLICT -> "ConflictError";
      case TOO_MANY_REQUESTS -> "RateLimitError";
      default -> "HttpError";
    };
  }
}
