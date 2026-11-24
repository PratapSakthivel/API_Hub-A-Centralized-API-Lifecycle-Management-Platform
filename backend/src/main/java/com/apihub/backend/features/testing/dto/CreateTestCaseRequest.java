package com.apihub.backend.features.testing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTestCaseRequest {
    @NotNull(message = "Test suite ID is required")
    private Long testSuiteId;

    private Long apiVersionId;

    @NotBlank(message = "Test name is required")
    private String name;

    @NotBlank(message = "Method is required")
    private String httpMethod;

    @NotBlank(message = "URL is required")
    private String endpointUrl;

    private String headersJson;
    private String requestBody;
    private Integer expectedStatusCode;
}
