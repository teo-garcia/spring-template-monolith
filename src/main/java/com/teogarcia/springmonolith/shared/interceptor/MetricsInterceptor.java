package com.teogarcia.springmonolith.shared.interceptor;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MetricsInterceptor implements HandlerInterceptor {

  private final MeterRegistry registry;

  public MetricsInterceptor(MeterRegistry registry) {
    this.registry = registry;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    String route = request.getRequestURI();
    // Cardinality guard: collapse unmatched routes to UNKNOWN (mirrors Adonis fix in PARITY.md)
    if (route.startsWith("/api/")) {
      // keep as-is for known API routes
    } else if (route.equals("/health")
        || route.startsWith("/health/")
        || route.equals("/metrics")
        || route.startsWith("/docs")) {
      // keep health/metrics/docs separate
    } else if (!route.equals("/")) {
      route = "UNKNOWN";
    }
    Counter.builder("http_requests_total")
        .tag("method", request.getMethod())
        .tag("route", route)
        .tag("status", String.valueOf(response.getStatus()))
        .register(registry)
        .increment();
    Long start = (Long) request.getAttribute("_start");
    if (start != null) {
      long nanos = System.nanoTime() - start;
      Timer.builder("http_request_duration_seconds")
          .tag("method", request.getMethod())
          .tag("route", route)
          .tag("status", String.valueOf(response.getStatus()))
          .publishPercentileHistogram(true)
          .register(registry)
          .record(Duration.ofNanos(nanos));
    }
  }
}
