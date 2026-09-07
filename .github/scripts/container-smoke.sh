#!/usr/bin/env bash

set -euo pipefail

image=${1:?container image is required}
network=spring-smoke

# Invoked by the EXIT trap.
# shellcheck disable=SC2329
cleanup() {
  docker rm --force spring-smoke-app spring-smoke-db 2>/dev/null || true
  docker network rm "$network" 2>/dev/null || true
}

trap cleanup EXIT

docker run --rm "$image" java -version
docker network create "$network"
docker run --detach --name spring-smoke-db \
  --network "$network" \
  --env POSTGRES_USER=postgres \
  --env POSTGRES_PASSWORD=postgres \
  --env POSTGRES_DB=spring_monolith \
  --volume "$PWD/src/main/resources/db/migration:/docker-entrypoint-initdb.d:ro" \
  postgres:18-alpine

for _ in $(seq 1 60); do
  if docker exec spring-smoke-db pg_isready -U postgres; then
    break
  fi
  sleep 1
done

if ! docker exec spring-smoke-db pg_isready -U postgres; then
  docker logs spring-smoke-db
  exit 1
fi

docker run --detach --name spring-smoke-app \
  --network "$network" \
  --publish 127.0.0.1:43118:3000 \
  --env APP_ENV=production \
  --env SPRING_PROFILES_ACTIVE=production \
  --env DATABASE_URL=jdbc:postgresql://spring-smoke-db:5432/spring_monolith \
  --env DATABASE_USER=postgres \
  --env DATABASE_PASSWORD=postgres \
  --env OTEL_ENABLED=false \
  "$image"

for _ in $(seq 1 60); do
  if curl --fail --silent http://127.0.0.1:43118/health/live >/dev/null; then
    exit 0
  fi
  sleep 1
done

docker logs spring-smoke-app
exit 1
