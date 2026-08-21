package com.teogarcia.springmonolith.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @NotBlank String env,
    @NotBlank String apiPrefix,
    @NotBlank String version,
    @Min(1000) long shutdownTimeout,
    boolean docsEnabled,
    @NotBlank String openapiServerUrl,
    boolean corsEnabled,
    @NotBlank String corsOrigin,
    @Min(1) int throttleTtl,
    @Min(1) int throttleLimit,
    boolean metricsEnabled,
    @NotBlank String logLevel,
    @NotBlank String logOutput,
    boolean otelEnabled,
    @NotBlank String otelServiceName,
    @NotBlank String otelEndpoint,
    @Min(1) int redisTtl) {}
