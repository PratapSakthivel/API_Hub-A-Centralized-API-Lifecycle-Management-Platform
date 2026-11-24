package com.apihub.backend.features.dashboard.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentActivityDto {
    private Long id;
    private String actorEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String details;
    private Instant timestamp;
}
