package com.teogarcia.springmonolith.shared.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorEnvelope(
    boolean success,
    int statusCode,
    String timestamp,
    String path,
    String method,
    Object message,
    String error,
    Map<String, Object> errors,
    Map<String, String> meta) {}
