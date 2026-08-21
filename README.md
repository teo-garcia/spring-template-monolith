<div align="center">

# Spring Template Monolith

**Production-ready Spring Boot monolith with JPA/Flyway, Redis, health checks, and metrics**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4-6DB33F?logo=springboot&logoColor=white)](https://spring.io)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-336791?logo=postgresql&logoColor=white)](https://postgresql.org)

Part of the [@teo-garcia/templates](https://github.com/teo-garcia/templates) ecosystem

</div>

---

## Features

| Category          | Technologies                                                      |
| ----------------- | ----------------------------------------------------------------- |
| **Framework**     | Spring Boot 3.4, Spring MVC, Bean Validation                      |
| **Database**      | PostgreSQL 18, Spring Data JPA (Hibernate), Flyway                |
| **Cache**         | Redis (Lettuce) + RedisTemplate                                   |
| **Observability** | Actuator health, Prometheus, Micrometer Tracing (OTel), Logstash  |
| **Type Safety**   | Java 21, `strict` validation, Checkstyle + Spotless               |
| **Testing**       | JUnit 5, MockMvc, Testcontainers, JaCoCo                          |
| **Code Quality**  | Spotless (google-java-format), Checkstyle, structured logging     |
| **DevOps**        | Docker (multi-stage), Compose, GitHub Actions CI/CD               |

---

## Requirements

- Java 21 (Temurin recommended)
- Maven 3.9+ (`./mvnw` wrapper included)
- Docker and Docker Compose
- PostgreSQL 18 + Redis (via Compose)

---

## Quick Start

```bash
./mvnw -B package -DskipTests
cp .env.example .env
cp .env.test.example .env.test
docker compose up -d db redis
./mvnw flyway:migrate
./mvnw spring-boot:run
```

The app starts on `http://localhost:3000`. API docs at `/docs` when `DOCS_ENABLED=true`.
OpenAPI JSON at `/openapi.json`.

---

## Scripts

| Command              | Description                              |
| -------------------- | ---------------------------------------- |
| `make dev`           | Start with Spring Boot dev (or `./mvnw spring-boot:run`) |
| `make build`         | Create production jar                    |
| `make check`         | Run lint + typecheck + format check + tests |
| `make start`         | Run production jar (`java -jar target/*.jar`) |
| `make test`          | Run unit tests                           |
| `make coverage`      | Run tests with JaCoCo (`target/site/jacoco`) |
| `make lint-check`    | Checkstyle + Spotless check              |
| `make format`        | Spotless apply (google-java-format)      |
| `make format-check`  | Spotless check                           |
| `make db-migrate`    | Create Flyway migration                  |
| `make db-deploy`     | Apply migrations (production)            |
| `make db-seed`       | Seed deterministic sample data (`--app.seed=true`) |
| `make docker-dev`    | `docker compose up -d db redis`          |

Maven equivalents: `./mvnw spring-boot:run`, `./mvnw verify`, `./mvnw checkstyle:check spotless:check`.

---

## API

- `GET /health`, `GET /health/live`, `GET /health/ready` — health (Actuator-backed)
- `GET /metrics` — Prometheus text
- `GET /docs`, `GET /openapi.json` — OpenAPI/Swagger UI (springdoc)
- `GET /api/v1/tasks?page=1&pageSize=20&status=PENDING&priority=1` — paginated list
- `POST /api/v1/tasks` — create
- `GET /api/v1/tasks/{id}` — get one
- `PATCH /api/v1/tasks/{id}` — update
- `DELETE /api/v1/tasks/{id}` — soft delete

Success envelope: `{success, statusCode, timestamp, path, method, data, meta{requestId, version}}`
Error envelope: `{success:false, statusCode, timestamp, path, method, message, error, errors, meta{requestId}}`
Paginated: `{data, meta:{total, page, pageSize}}`

---

## Migration Safety

Run Flyway migrations as a pre-deploy step with `make db-deploy` before the new version starts. Do not run migrations from app startup, request handlers, seed, or tests against a shared DB.

Production migrations must be backward-compatible. Expand-contract: add nullable columns/tables/indexes before code uses them; backfill explicitly; deploy code that stops reading old shape; then remove/narrow schema in a later release.

`make db-deploy` is idempotent. Rollback is backup restore + compatible code, or forward-fix migration. `make db-reset` (`flyway:clean`) is local/test-only.

---

## Environment

See `.env.example` — all vars validated at boot via `@ConfigurationProperties` (`AppProperties`). Fails fast on missing/invalid values. Grouped by sensitivity; `DATABASE_URL` is the canonical DB var.

---

## Observability

- JSON logs via `logstash-logback-encoder` (MDC `requestId` propagated by `RequestIdFilter`)
- `X-Request-ID` header round-trips; error `meta.requestId` mirrors it
- Micrometer Prometheus at `/metrics`, histogram buckets for p95/p99 (via `MetricsInterceptor`)
- OTel traces via `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp` (`OTEL_EXPORTER_OTLP_TRACES_ENDPOINT`)

---

## After creating from this template

1. Rename `com.teogarcia.springmonolith` package and `spring-template-monolith` artifact.
2. Update `APP_NAME`, `OTEL_SERVICE_NAME`, and `DATABASE_NAME` in `.env.example`.
3. Trim `Tasks` sample domain or replace with your bounded context.
4. Review `V1__create_tasks.sql` and add your migrations with `make db-migrate`.
5. Configure secrets per `GOVERNANCE.md` (env-only, no bake into image).
