package com.teogarcia.springmonolith.shared.health;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.teogarcia.springmonolith.config.AppProperties;

/**
 * Shared portfolio health contract, identical to the frontend templates' {@code lib/health.ts} and
 * the Nest, Adonis, Gin, FastAPI and Django backends:
 *
 * <pre>{status, timestamp, version, checks{name: "up"|"down"}}</pre>
 *
 * Actuator supplies the probes, but its {@code HealthDescriptor} shape is a framework artifact
 * rather than a contract anyone chose, so it is translated here. Health responses are never wrapped
 * in the success envelope: orchestrators parse them directly.
 */
@RestController
@RequestMapping("/health")
public class HealthController {

  private static final String CHECK_UP = "up";
  private static final String CHECK_DOWN = "down";

  private final HealthEndpoint healthEndpoint;
  private final AppProperties appProperties;

  public HealthController(HealthEndpoint healthEndpoint, AppProperties appProperties) {
    this.healthEndpoint = healthEndpoint;
    this.appProperties = appProperties;
  }

  /**
   * Liveness reports no checks: a dependency outage must not cause the orchestrator to restart an
   * otherwise healthy process.
   */
  @GetMapping("/live")
  public ResponseEntity<Map<String, Object>> live() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", "ok");
    body.put("timestamp", Instant.now().toString());
    body.put("version", appProperties.version());
    return ResponseEntity.ok(body);
  }

  /** Readiness and the overall check return the identical report. */
  @GetMapping("/ready")
  public ResponseEntity<Map<String, Object>> ready() {
    return report();
  }

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    return report();
  }

  private ResponseEntity<Map<String, Object>> report() {
    Map<String, String> checks = collectChecks();
    String status = resolveStatus(checks);

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status);
    body.put("timestamp", Instant.now().toString());
    body.put("version", appProperties.version());
    body.put("checks", checks);

    // Degraded and down both drain the instance.
    return ResponseEntity.status("ok".equals(status) ? 200 : 503).body(body);
  }

  /**
   * Actuator contributes many components (diskSpace, ssl, liveness/readiness state, ...). Only the
   * dependencies every template reports are surfaced, so `checks.database` and `checks.redis` mean
   * the same thing in every stack and a full disk cannot mark the service degraded here but nowhere
   * else.
   */
  private static final Map<String, String> REPORTED_COMPONENTS =
      Map.of("db", "database", "redis", "redis");

  private Map<String, String> collectChecks() {
    Map<String, String> checks = new TreeMap<>();
    HealthDescriptor descriptor = healthEndpoint.health();

    if (descriptor instanceof CompositeHealthDescriptor composite) {
      composite
          .getComponents()
          .forEach(
              (name, component) -> {
                String reported = REPORTED_COMPONENTS.get(name);
                if (reported != null) {
                  checks.put(
                      reported, isUp(component.getStatus().getCode()) ? CHECK_UP : CHECK_DOWN);
                }
              });
    }

    if (checks.isEmpty()) {
      checks.put("application", isUp(descriptor.getStatus().getCode()) ? CHECK_UP : CHECK_DOWN);
    }

    return checks;
  }

  private static boolean isUp(String code) {
    return "UP".equals(code);
  }

  /** Every check up -&gt; ok, some up -&gt; degraded, none up -&gt; down. */
  private static String resolveStatus(Map<String, String> checks) {
    if (checks.isEmpty()) {
      return "ok";
    }

    long up = checks.values().stream().filter(CHECK_UP::equals).count();

    if (up == checks.size()) {
      return "ok";
    }
    if (up == 0) {
      return "down";
    }
    return "degraded";
  }
}
