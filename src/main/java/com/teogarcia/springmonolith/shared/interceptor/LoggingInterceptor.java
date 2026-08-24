package com.teogarcia.springmonolith.shared.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoggingInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    MDC.put("method", request.getMethod());
    MDC.put("path", request.getRequestURI());
    request.setAttribute("_start", System.nanoTime());
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    Long start = (Long) request.getAttribute("_start");
    long durationMs = start != null ? (System.nanoTime() - start) / 1_000_000 : -1;
    log.info(
        "{} {} {} {}ms",
        request.getMethod(),
        request.getRequestURI(),
        response.getStatus(),
        durationMs);
    MDC.remove("method");
    MDC.remove("path");
  }
}
