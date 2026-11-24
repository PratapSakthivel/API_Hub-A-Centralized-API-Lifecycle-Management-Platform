package com.apihub.backend.features.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String projectKey;
    private String description;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
