package com.teogarcia.springmonolith.shared.interceptor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerMapping;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class MetricsInterceptorTest {

  @Test
  void usesMatchedRoutePatternInsteadOfConcreteResourceId() {
    SimpleMeterRegistry registry = new SimpleMeterRegistry();
    MetricsInterceptor interceptor = new MetricsInterceptor(registry);
    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/tasks/123");
    request.setAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE, "/api/v1/tasks/{id}");
    request.setAttribute("_start", System.nanoTime());
    MockHttpServletResponse response = new MockHttpServletResponse();
    response.setStatus(200);

    interceptor.afterCompletion(request, response, new Object(), null);

    assertThat(
            registry
                .get("http_requests_total")
                .tag("route", "/api/v1/tasks/{id}")
                .counter()
                .count())
        .isEqualTo(1.0);
    assertThat(registry.find("http_requests_total").tag("route", "/api/v1/tasks/123").counter())
        .isNull();
  }
}
