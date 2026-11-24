package com.apihub.backend.features.documentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateEndpointRequest {
    @NotBlank(message = "HTTP method is required")
    @Pattern(regexp = "^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)$", message = "Invalid HTTP method")
    private String httpMethod;

    @NotBlank(message = "Path is required")
    @Size(max = 255)
    private String path;

    @NotBlank(message = "Summary is required")
    @Size(max = 150)
    private String summary;

    private String description;
    private String operationId;
    private String requestSchema;
    private String responseSchema;
    private String expectedStatusCode;
}
