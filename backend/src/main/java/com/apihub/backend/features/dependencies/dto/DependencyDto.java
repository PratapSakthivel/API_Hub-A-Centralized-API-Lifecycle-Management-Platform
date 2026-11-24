package com.apihub.backend.features.dependencies.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DependencyDto {
    private Long id;
    private Long sourceApiId;
    private String sourceApiName;
    private String sourceApiSlug;
    private Long targetApiId;
    private String targetApiName;
    private String targetApiSlug;
    private String relationshipType;
    private String notes;
    private Instant createdAt;
}
