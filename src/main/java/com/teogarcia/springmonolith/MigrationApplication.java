package com.teogarcia.springmonolith;

import org.flywaydb.core.Flyway;

/**
 * Runs Flyway from the same immutable application image without starting an HTTP server.
 *
 * <p>The runner calls Flyway directly so migration order does not depend on the web application's
 * startup lifecycle.
 */
public final class MigrationApplication {

  private MigrationApplication() {}

  public static void main(String[] args) {
    Flyway.configure()
        .dataSource(
            requiredEnvironment("DATABASE_URL"),
            requiredEnvironment("DATABASE_USER"),
            requiredEnvironment("DATABASE_PASSWORD"))
        .locations("classpath:db/migration")
        .cleanDisabled(true)
        .load()
        .migrate();
  }

  private static String requiredEnvironment(String name) {
    String value = System.getenv(name);
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(name + " is required");
    }
    return value;
  }
}
