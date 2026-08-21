package com.teogarcia.springmonolith.modules.tasks.dto;

import com.teogarcia.springmonolith.modules.tasks.TaskStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record UpdateTaskRequest(
    @Size(max = 200) String title,
    @Size(max = 2000) String description,
    TaskStatus status,
    @Min(0) @Max(10) Integer priority) {}
