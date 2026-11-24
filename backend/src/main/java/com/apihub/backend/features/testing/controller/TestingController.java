package com.apihub.backend.features.testing.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.testing.domain.TestCase;
import com.apihub.backend.features.testing.domain.TestRun;
import com.apihub.backend.features.testing.domain.TestSuite;
import com.apihub.backend.features.testing.dto.CreateTestCaseRequest;
import com.apihub.backend.features.testing.dto.CreateTestSuiteRequest;
import com.apihub.backend.features.testing.service.TestExecutionService;
import com.apihub.backend.features.users.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TestingController {

    private final TestExecutionService testExecutionService;

    @GetMapping("/apis/{apiId}/test-suites")
    public ResponseEntity<ApiResponse<List<TestSuite>>> getSuites(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(testExecutionService.getSuitesByApi(apiId), "Test suites retrieved"));
    }

    @GetMapping("/test-suites/{suiteId}/cases")
    public ResponseEntity<ApiResponse<List<TestCase>>> getCases(@PathVariable Long suiteId) {
        return ResponseEntity.ok(ApiResponse.success(testExecutionService.getCasesBySuite(suiteId), "Test cases retrieved"));
    }

    @GetMapping("/apis/{apiId}/test-runs")
    public ResponseEntity<ApiResponse<Page<TestRun>>> getRuns(
            @PathVariable Long apiId,
            @PageableDefault(size = 10, sort = "startedAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(testExecutionService.getRunsByApi(apiId, pageable), "Test runs retrieved"));
    }

    @PostMapping("/test-suites")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'QA_ENGINEER')")
    public ResponseEntity<ApiResponse<TestSuite>> createSuite(
            @Valid @RequestBody CreateTestSuiteRequest request,
            @AuthenticationPrincipal User user
    ) {
        TestSuite created = testExecutionService.createSuite(request, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Test suite created"));
    }

    @PostMapping("/test-cases")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'QA_ENGINEER')")
    public ResponseEntity<ApiResponse<TestCase>> createCase(@Valid @RequestBody CreateTestCaseRequest request) {
        TestCase created = testExecutionService.createTestCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created, "Test case created"));
    }

    @PostMapping("/test-suites/{suiteId}/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER', 'QA_ENGINEER')")
    public ResponseEntity<ApiResponse<TestRun>> runSuite(
            @PathVariable Long suiteId,
            @AuthenticationPrincipal User user
    ) {
        TestRun run = testExecutionService.executeSuite(suiteId, user);
        return ResponseEntity.ok(ApiResponse.success(run, "Test suite executed"));
    }
}
