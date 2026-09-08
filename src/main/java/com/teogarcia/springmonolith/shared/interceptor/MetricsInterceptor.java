package com.teogarcia.springmonolith.shared.interceptor;

import java.time.Duration;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

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
    Object matchedPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String route = matchedPattern == null ? "UNKNOWN" : matchedPattern.toString();
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
