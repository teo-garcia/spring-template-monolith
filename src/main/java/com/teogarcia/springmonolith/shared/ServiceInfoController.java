package com.teogarcia.springmonolith.shared;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teogarcia.springmonolith.config.AppProperties;

/**
 * Root service-info endpoint. Every other template in the portfolio answers {@code GET /} with
 * {@code {name, status, version}}; this template previously had no root route at all, so {@code /}
 * raised NoResourceFoundException.
 */
@RestController
public class ServiceInfoController {

  private final AppProperties appProperties;
  private final String applicationName;

  public ServiceInfoController(
      AppProperties appProperties, @Value("${spring.application.name}") String applicationName) {
    this.appProperties = appProperties;
    this.applicationName = applicationName;
  }

  @GetMapping("/")
  public ResponseEntity<Map<String, Object>> serviceInfo() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("name", applicationName);
    body.put("status", "ok");
    body.put("version", appProperties.version());
    return ResponseEntity.ok(body);
  }
}
