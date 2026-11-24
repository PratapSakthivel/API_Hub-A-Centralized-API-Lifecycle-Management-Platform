package com.apihub.backend.features.dashboard;

import com.apihub.backend.features.audit.domain.AuditLog;
import com.apihub.backend.features.audit.repository.AuditLogRepository;
import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.dashboard.dto.DashboardSummaryDto;
import com.apihub.backend.features.dashboard.service.DashboardService;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.release.domain.ApiRelease;
import com.apihub.backend.features.release.domain.ReleaseStage;
import com.apihub.backend.features.release.repository.ApiReleaseRepository;
import com.apihub.backend.features.testing.domain.TestRun;
import com.apihub.backend.features.testing.repository.TestRunRepository;
import com.apihub.backend.features.testing.repository.TestSuiteRepository;
import com.apihub.backend.features.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TestSuiteRepository testSuiteRepository;

    @Mock
    private TestRunRepository testRunRepository;

    @Mock
    private ApiReleaseRepository apiReleaseRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummary_CalculatesAccurateMetrics() {
        when(apiRepository.count()).thenReturn(5L);
        when(projectRepository.count()).thenReturn(2L);
        when(userRepository.count()).thenReturn(10L);
        when(testSuiteRepository.count()).thenReturn(3L);

        // Test runs: 3 runs, 2 passed, 1 failed
        TestRun run1 = TestRun.builder().overallStatus("PASSED").build();
        TestRun run2 = TestRun.builder().overallStatus("PASSED").build();
        TestRun run3 = TestRun.builder().overallStatus("FAILED").build();
        when(testRunRepository.findAll()).thenReturn(List.of(run1, run2, run3));

        // APIs: 2 ACTIVE, 1 DRAFT
        ApiEntity api1 = ApiEntity.builder().lifecycleStatus(LifecycleStatus.ACTIVE).build();
        ApiEntity api2 = ApiEntity.builder().lifecycleStatus(LifecycleStatus.ACTIVE).build();
        ApiEntity api3 = ApiEntity.builder().lifecycleStatus(LifecycleStatus.DRAFT).build();
        when(apiRepository.findAll()).thenReturn(List.of(api1, api2, api3));

        // Releases: 1 PRODUCTION, 1 STAGING
        ApiRelease rel1 = new ApiRelease();
        rel1.setStage(ReleaseStage.PRODUCTION);
        ApiRelease rel2 = new ApiRelease();
        rel2.setStage(ReleaseStage.STAGING);
        when(apiReleaseRepository.findAll()).thenReturn(List.of(rel1, rel2));

        // Audit log
        AuditLog log = AuditLog.builder()
                .id(1L)
                .actorEmail("dev@apihub.com")
                .action("API_CREATED")
                .entityType("API")
                .entityId("1")
                .createdAt(Instant.now())
                .build();
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(log)));

        DashboardSummaryDto summary = dashboardService.getSummary();

        assertNotNull(summary);
        assertEquals(5L, summary.getTotalApis());
        assertEquals(2L, summary.getTotalProjects());
        assertEquals(10L, summary.getTotalUsers());
        assertEquals(3L, summary.getTotalTestSuites());
        assertEquals(3L, summary.getTotalTestRuns());
        assertEquals(2L, summary.getPassedTestRuns());
        assertEquals(1L, summary.getFailedTestRuns());
        assertEquals(66.7, summary.getTestPassRatePercentage(), 0.1);

        assertEquals(2L, summary.getApisByLifecycleStatus().get("ACTIVE"));
        assertEquals(1L, summary.getApisByLifecycleStatus().get("DRAFT"));
        assertEquals(1L, summary.getReleasesByStage().get("PRODUCTION"));
        assertEquals(1L, summary.getReleasesByStage().get("STAGING"));
        assertEquals(1, summary.getRecentActivity().size());
        assertEquals("dev@apihub.com", summary.getRecentActivity().get(0).getActorEmail());
    }

    @Test
    void getSummary_EmptyData_HandlesZeroSafely() {
        when(apiRepository.count()).thenReturn(0L);
        when(projectRepository.count()).thenReturn(0L);
        when(userRepository.count()).thenReturn(0L);
        when(testSuiteRepository.count()).thenReturn(0L);
        when(testRunRepository.findAll()).thenReturn(Collections.emptyList());
        when(apiRepository.findAll()).thenReturn(Collections.emptyList());
        when(apiReleaseRepository.findAll()).thenReturn(Collections.emptyList());
        when(auditLogRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        DashboardSummaryDto summary = dashboardService.getSummary();

        assertNotNull(summary);
        assertEquals(0L, summary.getTotalApis());
        assertEquals(0L, summary.getTotalTestRuns());
        assertEquals(0.0, summary.getTestPassRatePercentage());
        assertEquals(0, summary.getRecentActivity().size());
    }
}
