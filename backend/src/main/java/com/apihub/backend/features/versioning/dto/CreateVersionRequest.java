package com.apihub.backend.features.versioning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateVersionRequest {
    @NotBlank(message = "Version label is required")
    @Pattern(regexp = "^v?\\d+\\.\\d+\\.\\d+$", message = "Version label must follow semantic versioning (e.g. v1.0.0 or 1.0.0)")
    private String versionLabel;

    private String changeSummary;
}
