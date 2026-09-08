package com.teogarcia.springmonolith.shared.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    MDC.put("method", request.getMethod());
    request.setAttribute("_start", System.nanoTime());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Long start = (Long) request.getAttribute("_start");
    long durationMs = start != null ? (System.nanoTime() - start) / 1_000_000 : -1;
    Object matchedPattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
    String route = matchedPattern == null ? "UNKNOWN" : matchedPattern.toString();
    SpanContext spanContext = Span.current().getSpanContext();
    try {
      MDC.put("route", route);
      MDC.put("status", String.valueOf(response.getStatus()));
      MDC.put("duration_ms", String.valueOf(durationMs));
      if (spanContext.isValid()) {
        MDC.put("trace_id", spanContext.getTraceId());
        MDC.put("span_id", spanContext.getSpanId());
      }
      log.info("{} {} {} {}ms", request.getMethod(), route, response.getStatus(), durationMs);
    } finally {
      MDC.remove("method");
      MDC.remove("route");
      MDC.remove("status");
      MDC.remove("duration_ms");
      MDC.remove("trace_id");
      MDC.remove("span_id");
    }
  }
}
