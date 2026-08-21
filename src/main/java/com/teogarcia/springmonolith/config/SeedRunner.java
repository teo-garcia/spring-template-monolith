package com.teogarcia.springmonolith.config;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.teogarcia.springmonolith.modules.tasks.Task;
import com.teogarcia.springmonolith.modules.tasks.TaskRepository;
import com.teogarcia.springmonolith.modules.tasks.TaskStatus;

/**
 * Deterministic seed for local dev / e2e. Run with: ./mvnw -Dseed=true spring-boot:run or: java
 * -jar target/*.jar --app.seed=true Mirrors prisma/seed.ts and app/seed.py.
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
public class SeedRunner implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(SeedRunner.class);
  private final TaskRepository repo;

  public SeedRunner(TaskRepository repo) {
    this.repo = repo;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    repo.deleteAll();
    List<Task> seeds =
        List.of(
            new Task(
                uid(),
                "Review onboarding checklist",
                "Confirm the template health, docs, metrics, and task APIs.",
                TaskStatus.PENDING,
                3),
            new Task(
                uid(),
                "Ship API contract polish",
                "Validate pagination, errors, and OpenAPI schema examples.",
                TaskStatus.IN_PROGRESS,
                7),
            new Task(
                uid(),
                "Archive completed setup",
                "Keep a completed task available for filtering examples.",
                TaskStatus.COMPLETED,
                1));
    repo.saveAll(seeds);
    log.info("Seeded {} tasks", seeds.size());
  }

  private String uid() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
  }
}
