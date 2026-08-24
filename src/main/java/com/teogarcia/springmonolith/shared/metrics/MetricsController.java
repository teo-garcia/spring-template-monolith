package com.teogarcia.springmonolith.shared.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;

@RestController
@ConditionalOnProperty(name = "app.metrics-enabled", havingValue = "true", matchIfMissing = true)
public class MetricsController {

  private final PrometheusMeterRegistry registry;

  public MetricsController(PrometheusMeterRegistry registry) {
    this.registry = registry;
  }

  @GetMapping(value = "/metrics", produces = "text/plain; version=0.0.4; charset=utf-8")
  public ResponseEntity<String> metrics() {
    return ResponseEntity.ok()
        .header("Content-Type", "text/plain; version=0.0.4; charset=utf-8")
        .body(registry.scrape());
  }
}
