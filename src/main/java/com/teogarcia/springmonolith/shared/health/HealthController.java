package com.teogarcia.springmonolith.shared.health;

import java.util.Map;

import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Portfolio health contract: GET /health, /health/live, /health/ready Wraps Actuator HealthEndpoint
 * to keep one source of truth.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

  private final HealthEndpoint healthEndpoint;

  public HealthController(HealthEndpoint healthEndpoint) {
    this.healthEndpoint = healthEndpoint;
  }

  @GetMapping("/live")
  public ResponseEntity<Map<String, String>> live() {
    return ResponseEntity.ok(Map.of("status", "ok"));
  }

  @GetMapping("/ready")
  public ResponseEntity<HealthDescriptor> ready() {
    HealthDescriptor health = healthEndpoint.health();
    if (health.getStatus().getCode().equals("UP")) {
      return ResponseEntity.ok(health);
    }
    return ResponseEntity.status(503).body(health);
  }

  @GetMapping
  public ResponseEntity<HealthDescriptor> health() {
    HealthDescriptor health = healthEndpoint.health();
    // Align degraded semantics: DOWN -> degraded/503, UP -> 200
    if (health.getStatus().getCode().equals("UP")) {
      return ResponseEntity.ok(health);
    }
    return ResponseEntity.status(503).body(health);
  }
}
