.PHONY: dev build start start-prod lint lint-check format format-check test coverage check db-migrate db-deploy db-seed docker-dev

dev:
	./mvnw spring-boot:run

build:
	./mvnw -B package -DskipTests

start:
	java -jar target/*.jar

start-prod:
	java -jar target/*.jar

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
	./mvnw -B verify

check: lint-check format-check test
	./mvnw -B verify -DskipTests=false

db-migrate:
	./mvnw -B flyway:migrate

db-deploy:
	./mvnw -B flyway:migrate

db-seed:
	./mvnw -B spring-boot:run -Dspring-boot.run.arguments="--app.seed=true"

db-reset:
	./mvnw -B flyway:clean -Dflyway.cleanDisabled=false && ./mvnw -B flyway:migrate

docker-dev:
	docker compose up -d db redis

docker-build:
	docker build -f docker/Dockerfile -t spring-template-monolith .
