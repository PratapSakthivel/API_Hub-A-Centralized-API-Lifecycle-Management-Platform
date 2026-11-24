package com.apihub.backend.features.dependencies.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDependencyRequest {
    @NotNull(message = "Source API ID is required")
    private Long sourceApiId;

    @NotNull(message = "Target API ID is required")
    private Long targetApiId;

    private String relationshipType;
    private String notes;
}
