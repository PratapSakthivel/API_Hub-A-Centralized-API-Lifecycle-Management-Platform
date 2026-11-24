package com.apihub.backend.features.dashboard.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryDto {
    private long totalApis;
    private long totalProjects;
    private long totalUsers;
    private long totalTestSuites;
    private long totalTestRuns;
    private long passedTestRuns;
    private long failedTestRuns;
    private double testPassRatePercentage;
    private Map<String, Long> apisByLifecycleStatus;
    private Map<String, Long> releasesByStage;
    private List<RecentActivityDto> recentActivity;
}
