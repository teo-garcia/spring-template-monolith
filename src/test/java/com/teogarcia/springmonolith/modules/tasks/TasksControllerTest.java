package com.teogarcia.springmonolith.modules.tasks;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TasksControllerTest {

  @Autowired MockMvc mockMvc;

  @Test
  void healthLiveReturnsOk() throws Exception {
    mockMvc
        .perform(get("/health/live").header("X-Request-ID", "health-check"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ok"))
        .andExpect(jsonPath("$.success").doesNotExist())
        .andExpect(header().string("X-Request-ID", "health-check"))
        .andExpect(header().string("X-Content-Type-Options", "nosniff"))
        .andExpect(header().string("X-Frame-Options", "DENY"));
  }

  @Test
  void operationalEndpointsExposeRealDocumentsAndMetrics() throws Exception {
    mockMvc
        .perform(get("/openapi.json"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.openapi").exists())
        .andExpect(jsonPath("$.paths['/api/v1/tasks']").exists())
        .andExpect(jsonPath("$.paths['/api/v1/tasks'].post.responses['201']").exists())
        .andExpect(jsonPath("$.paths['/api/v1/tasks'].post.responses['200']").doesNotExist())
        .andExpect(jsonPath("$.paths['/api/v1/tasks/{id}'].delete.responses['204']").exists());

    mockMvc
        .perform(get("/metrics"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("# HELP")));
  }

  @Test
  void createAndListTasks() throws Exception {
    String body =
        """
        {"title":"Test task","description":"from test","priority":5}
        """;
    mockMvc
        .perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.statusCode").value(201))
        .andExpect(jsonPath("$.path").value("/api/v1/tasks"))
        .andExpect(jsonPath("$.meta.version").value("1"))
        .andExpect(jsonPath("$.data.title").value("Test task"))
        .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
        .andExpect(jsonPath("$.data.updatedAt").isNotEmpty());

    mockMvc
        .perform(get("/api/v1/tasks").param("page", "1").param("pageSize", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.data").isArray())
        .andExpect(jsonPath("$.data.meta.total").isNumber());

    // validation error → 422 with ValidationError envelope (not wrapped)
    mockMvc
        .perform(
            post("/api/v1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
        .andExpect(status().isUnprocessableContent())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("ValidationError"));

    mockMvc
        .perform(post("/api/v1/tasks").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("BadRequestError"));
  }

  @Test
  void notFoundReturnsEnvelope() throws Exception {
    mockMvc
        .perform(get("/api/v1/tasks/does-not-exist"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("NotFoundError"))
        .andExpect(header().exists("X-Request-ID"));
  }
}
