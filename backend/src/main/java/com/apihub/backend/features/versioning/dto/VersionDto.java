package com.apihub.backend.features.versioning.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionDto {
    private Long id;
    private Long apiId;
    private String apiName;
    private String versionLabel;
    private String status;
    private String changeSummary;
    private Long createdById;
    private String createdByName;
    private Instant publishedAt;
    private Instant createdAt;
    private Instant updatedAt;
}
