package com.apihub.backend.features.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTestSuiteRequest {
    @NotNull(message = "API ID is required")
    private Long apiId;

    @NotBlank(message = "Suite name is required")
    private String name;

    private String description;
}
