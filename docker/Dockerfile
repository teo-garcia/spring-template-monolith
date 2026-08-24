# syntax=docker/dockerfile:1.7

FROM maven:3.9-eclipse-temurin-25-alpine AS builder
WORKDIR /app
COPY pom.xml ./
COPY checkstyle.xml ./
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:25-jre-alpine AS production
RUN apk add --no-cache dumb-init wget

RUN addgroup -g 1001 -S spring && adduser -S spring -u 1001

WORKDIR /app
COPY --from=builder /app/target/spring-template-monolith-*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 3000
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:${PORT:-3000}/health/live | grep -q ok || exit 1

ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "--enable-native-access=ALL-UNNAMED", "--sun-misc-unsafe-memory-access=allow", "-jar", "app.jar"]
