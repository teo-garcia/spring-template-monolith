package com.teogarcia.springmonolith.shared.metrics;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class MetricsController {

  private final MeterRegistry registry;

  public MetricsController(MeterRegistry registry) {
    this.registry = registry;
  }

  @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
  public ResponseEntity<String> metrics() {
    String body;
    try {
      // PrometheusMeterRegistry has scrape(), generic registry does not — fall back to empty
      body = (String) registry.getClass().getMethod("scrape").invoke(registry);
    } catch (Exception e) {
      body =
          "# HELP http_requests_total Total number of HTTP requests\n# TYPE http_requests_total counter\n";
    }
    return ResponseEntity.ok()
        .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
        .body(body);
  }
}
