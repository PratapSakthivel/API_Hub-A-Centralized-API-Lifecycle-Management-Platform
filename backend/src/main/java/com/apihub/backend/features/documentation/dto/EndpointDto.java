package com.apihub.backend.features.documentation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointDto {
    private Long id;
    private Long apiVersionId;
    private String httpMethod;
    private String path;
    private String summary;
    private String description;
    private String operationId;
    private String requestSchema;
    private String responseSchema;
    private String expectedStatusCode;
    private Instant createdAt;
    private Instant updatedAt;
}
