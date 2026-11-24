package com.apihub.backend.features.dashboard.service;

import com.apihub.backend.features.audit.domain.AuditLog;
import com.apihub.backend.features.audit.repository.AuditLogRepository;
import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.dashboard.dto.DashboardSummaryDto;
import com.apihub.backend.features.dashboard.dto.RecentActivityDto;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.release.domain.ApiRelease;
import com.apihub.backend.features.release.domain.ReleaseStage;
import com.apihub.backend.features.release.repository.ApiReleaseRepository;
import com.apihub.backend.features.testing.domain.TestRun;
import com.apihub.backend.features.testing.repository.TestRunRepository;
import com.apihub.backend.features.testing.repository.TestSuiteRepository;
import com.apihub.backend.features.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ApiRepository apiRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestRunRepository testRunRepository;
    private final ApiReleaseRepository apiReleaseRepository;
    private final AuditLogRepository auditLogRepository;

    public DashboardSummaryDto getSummary() {
        long totalApis = apiRepository.count();
        long totalProjects = projectRepository.count();
        long totalUsers = userRepository.count();
        long totalTestSuites = testSuiteRepository.count();

        List<TestRun> allRuns = testRunRepository.findAll();
        long totalTestRuns = allRuns.size();
        long passedTestRuns = allRuns.stream()
                .filter(run -> "PASSED".equalsIgnoreCase(run.getOverallStatus()))
                .count();
        long failedTestRuns = allRuns.stream()
                .filter(run -> "FAILED".equalsIgnoreCase(run.getOverallStatus()))
                .count();

        double testPassRate = totalTestRuns > 0
                ? Math.round(((double) passedTestRuns / totalTestRuns) * 1000.0) / 10.0
                : 0.0;

        // Group APIs by LifecycleStatus
        Map<String, Long> apisByStatus = new LinkedHashMap<>();
        for (LifecycleStatus status : LifecycleStatus.values()) {
            apisByStatus.put(status.name(), 0L);
        }
        for (ApiEntity api : apiRepository.findAll()) {
            if (api.getLifecycleStatus() != null) {
                String key = api.getLifecycleStatus().name();
                apisByStatus.put(key, apisByStatus.getOrDefault(key, 0L) + 1L);
            }
        }

        // Group Releases by Stage
        Map<String, Long> releasesByStage = new LinkedHashMap<>();
        for (ReleaseStage stage : ReleaseStage.values()) {
            releasesByStage.put(stage.name(), 0L);
        }
        for (ApiRelease release : apiReleaseRepository.findAll()) {
            if (release.getStage() != null) {
                String key = release.getStage().name();
                releasesByStage.put(key, releasesByStage.getOrDefault(key, 0L) + 1L);
            }
        }

        // Recent Audit logs
        Page<AuditLog> recentLogsPage = auditLogRepository.findAll(
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "createdAt"))
        );

        List<RecentActivityDto> recentActivity = recentLogsPage.getContent().stream()
                .map(log -> RecentActivityDto.builder()
                        .id(log.getId())
                        .actorEmail(log.getActorEmail())
                        .action(log.getAction())
                        .entityType(log.getEntityType())
                        .entityId(log.getEntityId())
                        .details(log.getDetails())
                        .timestamp(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardSummaryDto.builder()
                .totalApis(totalApis)
                .totalProjects(totalProjects)
                .totalUsers(totalUsers)
                .totalTestSuites(totalTestSuites)
                .totalTestRuns(totalTestRuns)
                .passedTestRuns(passedTestRuns)
                .failedTestRuns(failedTestRuns)
                .testPassRatePercentage(testPassRate)
                .apisByLifecycleStatus(apisByStatus)
                .releasesByStage(releasesByStage)
                .recentActivity(recentActivity)
                .build();
    }
}
