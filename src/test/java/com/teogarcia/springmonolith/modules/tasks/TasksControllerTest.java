package com.teogarcia.springmonolith.modules.tasks;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
    mockMvc.perform(get("/health/live")).andExpect(status().isOk());
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
        .andExpect(jsonPath("$.data.title").value("Test task"));

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
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.error").value("ValidationError"));
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
