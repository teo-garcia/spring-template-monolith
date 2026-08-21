package com.teogarcia.springmonolith.shared.filter;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Propagates X-Request-ID through the request lifecycle. If the header is absent, a UUID is
 * generated. The id is exposed as request attribute "requestId", response header, and MDC key
 * "requestId".
 */
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Request-ID";
  public static final String MDC_KEY = "requestId";
  public static final String ATTR = "requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String requestId = request.getHeader(HEADER);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }
    MDC.put(MDC_KEY, requestId);
    request.setAttribute(ATTR, requestId);
    response.setHeader(HEADER, requestId);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
