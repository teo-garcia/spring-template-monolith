.PHONY: dev build start start-prod lint lint-check format format-check test coverage check db-migrate db-deploy db-seed docker-dev

ENV_FILE ?= .env
RUN_WITH_ENV = set -e; set -a; [ ! -f "$(ENV_FILE)" ] || . "./$(ENV_FILE)"; set +a;

dev:
	@$(RUN_WITH_ENV) exec ./mvnw -q spring-boot:run

build:
	./mvnw -B package -DskipTests

start:
	@$(RUN_WITH_ENV) exec java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -jar target/*.jar

start-prod:
	@$(RUN_WITH_ENV) exec java --enable-native-access=ALL-UNNAMED --sun-misc-unsafe-memory-access=allow -jar target/*.jar

lint: lint-check

lint-check:
	./mvnw -B checkstyle:check spotless:check

format:
	./mvnw -B spotless:apply

format-check:
	./mvnw -B spotless:check

test:
	./mvnw -B test

coverage:
	./mvnw -B verify -Pcoverage

check: coverage

db-migrate:
	@$(RUN_WITH_ENV) ./mvnw -B flyway:migrate

db-deploy:
	@$(RUN_WITH_ENV) ./mvnw -B flyway:migrate

db-seed:
	@$(RUN_WITH_ENV) ./mvnw -B spring-boot:run -Dspring-boot.run.arguments="--app.seed=true"

db-reset:
	@$(RUN_WITH_ENV) ./mvnw -B flyway:clean -Dflyway.cleanDisabled=false && ./mvnw -B flyway:migrate

docker-dev:
	docker compose up --build

docker-build:
	docker build -f docker/Dockerfile -t spring-template-monolith .
