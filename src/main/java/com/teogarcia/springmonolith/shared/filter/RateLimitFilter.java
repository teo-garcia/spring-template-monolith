package com.teogarcia.springmonolith.shared.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.teogarcia.springmonolith.config.AppProperties;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Simple in-memory fixed-window rate limiter. Mirrors Nest ThrottlerGuard semantics: ~100 req/min
 * default (configurable via THROTTLE_LIMIT/THROTTLE_TTL). Paths /health*, /metrics, /docs* and /
 * are skipped. Replace with a Redis-backed Bucket4j implementation without changing the contract
 * when a distributed limiter is needed.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

  private final AppProperties props;
  private final ObjectMapper mapper;
  private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

  public RateLimitFilter(AppProperties props, ObjectMapper mapper) {
    this.props = props;
    this.mapper = mapper;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String path = request.getRequestURI();
    if (shouldSkip(path)) {
      chain.doFilter(request, response);
      return;
    }
    String key = request.getRemoteAddr();
    Window window = windows.computeIfAbsent(key, k -> new Window());
    long nowSeconds = Instant.now().getEpochSecond();
    long windowStart = window.start.get();
    if (nowSeconds - windowStart >= props.throttleTtl()) {
      window.start.set(nowSeconds);
      window.count.set(0);
      windowStart = nowSeconds;
    }
    int count = window.count.incrementAndGet();
    response.setHeader("X-RateLimit-Limit", String.valueOf(props.throttleLimit()));
    response.setHeader(
        "X-RateLimit-Remaining", String.valueOf(Math.max(0, props.throttleLimit() - count)));
    response.setHeader("X-RateLimit-Reset", String.valueOf(windowStart + props.throttleTtl()));
    if (count <= props.throttleLimit()) {
      chain.doFilter(request, response);
    } else {
      response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      String requestId = (String) request.getAttribute(RequestIdFilter.ATTR);
      Map<String, Object> body =
          Map.of(
              "success",
              false,
              "statusCode",
              429,
              "timestamp",
              Instant.now().toString(),
              "path",
              request.getRequestURI(),
              "method",
              request.getMethod(),
              "message",
              "Too many requests",
              "error",
              "RateLimitError",
              "meta",
              requestId != null ? Map.of("requestId", requestId) : Map.of());
      response.getWriter().write(mapper.writeValueAsString(body));
    }
  }

  private boolean shouldSkip(String path) {
    return path.equals("/")
        || path.equals("/health")
        || path.startsWith("/health/")
        || path.equals("/metrics")
        || path.equals("/docs")
        || path.startsWith("/docs/")
        || path.equals("/openapi.json")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui");
  }

  private static class Window {
    final AtomicLong start = new AtomicLong(Instant.now().getEpochSecond());
    final AtomicInteger count = new AtomicInteger(0);
  }
}
