package com.apihub.backend.features.environments.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEnvironmentRequest {
    private Long apiId;
    private String name;
    private String code; // DEV, STAGING, PRODUCTION, SANDBOX
    private String baseUrl;
    private String description;
    private List<EnvironmentVariableDto> variables;
}
