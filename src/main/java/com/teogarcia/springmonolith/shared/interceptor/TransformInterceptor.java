package com.teogarcia.springmonolith.shared.interceptor;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.teogarcia.springmonolith.config.AppProperties;
import com.teogarcia.springmonolith.shared.filter.RequestIdFilter;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Wraps every non-health/metrics/docs response in the portfolio success envelope: {success,
 * statusCode, timestamp, path, method, data, meta{requestId, version, duration}} Mirrors Nest
 * TransformInterceptor.
 */
@RestControllerAdvice
public class TransformInterceptor implements ResponseBodyAdvice<Object> {

  private final AppProperties props;

  public TransformInterceptor(AppProperties props) {
    this.props = props;
  }

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converter) {
    return true;
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    String path = request.getURI().getPath();
    if (path.equals("/metrics")
        || path.startsWith("/health")
        || path.startsWith("/docs")
        || path.equals("/openapi.json")
        || path.startsWith("/v3/api-docs")
        || path.startsWith("/swagger-ui")
        || path.startsWith("/actuator")) {
      return body;
    }
    // If body is already an envelope (error path), don't double-wrap
    if (body instanceof Map<?, ?> m && m.containsKey("success")) {
      return body;
    }
    if (body instanceof com.teogarcia.springmonolith.shared.exception.ErrorEnvelope) {
      return body;
    }
    HttpServletRequest servletRequest = null;
    String requestId = null;
    String method = "GET";
    if (request instanceof ServletServerHttpRequest ssr) {
      servletRequest = ssr.getServletRequest();
      Object rid = servletRequest.getAttribute(RequestIdFilter.ATTR);
      requestId = rid instanceof String s ? s : null;
      method = servletRequest.getMethod();
      path = servletRequest.getRequestURI();
      String qs = servletRequest.getQueryString();
      if (qs != null) path = path + "?" + qs;
    }
    Map<String, Object> envelope = new LinkedHashMap<>();
    envelope.put("success", true);
    int statusCode = 200;
    if (response instanceof org.springframework.http.server.ServletServerHttpResponse ssr2) {
      statusCode = ssr2.getServletResponse().getStatus();
      if (statusCode == 0) statusCode = 200;
    }
    envelope.put("statusCode", statusCode);
    envelope.put("timestamp", Instant.now().toString());
    envelope.put("path", path);
    envelope.put("method", method);
    envelope.put("data", body);
    Map<String, Object> meta = new LinkedHashMap<>();
    if (requestId != null) meta.put("requestId", requestId);
    meta.put("version", props.version());
    envelope.put("meta", meta);
    return envelope;
  }
}
