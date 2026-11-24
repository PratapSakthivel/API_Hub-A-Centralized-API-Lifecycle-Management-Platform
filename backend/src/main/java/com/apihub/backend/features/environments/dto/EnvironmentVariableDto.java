package com.apihub.backend.features.environments.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentVariableDto {
    private Long id;
    private String key;
    private String value;
    private boolean secret;
}
