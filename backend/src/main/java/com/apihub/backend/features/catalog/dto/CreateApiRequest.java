package com.apihub.backend.features.catalog.dto;

import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateApiRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;

    @NotBlank(message = "API name is required")
    @Size(min = 2, max = 150)
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(min = 2, max = 150)
    private String slug;

    private String summary;
    private String description;
    private LifecycleStatus lifecycleStatus;
}
