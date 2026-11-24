package com.apihub.backend.features.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProjectRequest {
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100)
    private String name;

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 20)
    private String projectKey;

    private String description;
}
