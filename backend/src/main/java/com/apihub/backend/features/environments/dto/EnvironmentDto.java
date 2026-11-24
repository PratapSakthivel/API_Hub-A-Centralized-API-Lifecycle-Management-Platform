package com.apihub.backend.features.environments.dto;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentDto {
    private Long id;
    private Long apiId;
    private String apiName;
    private String name;
    private String code;
    private String baseUrl;
    private String description;
    private boolean active;
    private List<EnvironmentVariableDto> variables;
    private Instant createdAt;
}
