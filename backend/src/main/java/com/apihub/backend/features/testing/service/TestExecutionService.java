package com.apihub.backend.features.testing.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.testing.domain.TestCase;
import com.apihub.backend.features.testing.domain.TestResult;
import com.apihub.backend.features.testing.domain.TestRun;
import com.apihub.backend.features.testing.domain.TestSuite;
import com.apihub.backend.features.testing.dto.CreateTestCaseRequest;
import com.apihub.backend.features.testing.dto.CreateTestSuiteRequest;
import com.apihub.backend.features.testing.dto.HttpExecutionResult;
import com.apihub.backend.features.testing.repository.TestCaseRepository;
import com.apihub.backend.features.testing.repository.TestRunRepository;
import com.apihub.backend.features.testing.repository.TestSuiteRepository;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestExecutionService {

    private final TestSuiteRepository testSuiteRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestRunRepository testRunRepository;
    private final ApiRepository apiRepository;
    private final ApiVersionRepository versionRepository;
    private final SafeHttpExecutor httpExecutor;

    public List<TestSuite> getSuitesByApi(Long apiId) {
        return testSuiteRepository.findByApiId(apiId);
    }

    public List<TestCase> getCasesBySuite(Long suiteId) {
        return testCaseRepository.findByTestSuiteId(suiteId);
    }

    public Page<TestRun> getRunsByApi(Long apiId, Pageable pageable) {
        return testRunRepository.findByApiIdOrderByStartedAtDesc(apiId, pageable);
    }

    @Transactional
    public TestSuite createSuite(CreateTestSuiteRequest req, User user) {
        ApiEntity api = apiRepository.findById(req.getApiId())
                .orElseThrow(() -> new IllegalArgumentException("API not found: " + req.getApiId()));

        TestSuite suite = TestSuite.builder()
                .api(api)
                .name(req.getName().trim())
                .description(req.getDescription())
                .createdBy(user)
                .build();

        return testSuiteRepository.save(suite);
    }

    @Transactional
    public TestCase createTestCase(CreateTestCaseRequest req) {
        TestSuite suite = testSuiteRepository.findById(req.getTestSuiteId())
                .orElseThrow(() -> new IllegalArgumentException("Suite not found: " + req.getTestSuiteId()));

        ApiVersion version = null;
        if (req.getApiVersionId() != null) {
            version = versionRepository.findById(req.getApiVersionId()).orElse(null);
        }

        TestCase tc = TestCase.builder()
                .testSuite(suite)
                .apiVersion(version)
                .name(req.getName().trim())
                .httpMethod(req.getHttpMethod().toUpperCase().trim())
                .endpointUrl(req.getEndpointUrl().trim())
                .headersJson(req.getHeadersJson())
                .requestBody(req.getRequestBody())
                .expectedStatusCode(req.getExpectedStatusCode() != null ? req.getExpectedStatusCode() : 200)
                .enabled(true)
                .build();

        return testCaseRepository.save(tc);
    }

    @Transactional
    public TestRun executeSuite(Long suiteId, User triggeredBy) {
        TestSuite suite = testSuiteRepository.findById(suiteId)
                .orElseThrow(() -> new IllegalArgumentException("Suite not found: " + suiteId));

        List<TestCase> cases = testCaseRepository.findByTestSuiteId(suiteId);

        TestRun run = TestRun.builder()
                .api(suite.getApi())
                .testSuite(suite)
                .triggeredBy(triggeredBy)
                .overallStatus("RUNNING")
                .totalTests(cases.size())
                .results(new ArrayList<>())
                .startedAt(Instant.now())
                .build();

        int passed = 0;
        int failed = 0;
        long totalDuration = 0;

        for (TestCase tc : cases) {
            if (!tc.isEnabled()) continue;

            HttpExecutionResult res = httpExecutor.execute(
                    tc.getHttpMethod(),
                    tc.getEndpointUrl(),
                    tc.getRequestBody(),
                    tc.getExpectedStatusCode()
            );

            totalDuration += res.getDurationMs();
            if (res.isSuccessful()) {
                passed++;
            } else {
                failed++;
            }

            TestResult tr = TestResult.builder()
                    .testRun(run)
                    .testCase(tc)
                    .testCaseName(tc.getName())
                    .expectedStatus(tc.getExpectedStatusCode())
                    .actualStatus(res.getStatusCode())
                    .durationMs(res.getDurationMs())
                    .status(res.isSuccessful() ? "PASSED" : "FAILED")
                    .sanitizedResponse(res.getResponseBody())
                    .failureMessage(res.getFailureReason())
                    .build();

            run.getResults().add(tr);
        }

        run.setPassedTests(passed);
        run.setFailedTests(failed);
        run.setOverallStatus(failed > 0 ? "FAILED" : "PASSED");
        run.setDurationMs(totalDuration);
        run.setCompletedAt(Instant.now());

        return testRunRepository.save(run);
    }
}
