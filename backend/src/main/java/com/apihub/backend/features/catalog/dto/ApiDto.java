package com.apihub.backend.features.catalog.dto;

import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private String projectKey;
    private String name;
    private String slug;
    private String summary;
    private String description;
    private Long ownerId;
    private String ownerName;
    private LifecycleStatus lifecycleStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
