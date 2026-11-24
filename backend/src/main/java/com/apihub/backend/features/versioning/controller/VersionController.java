package com.apihub.backend.features.versioning.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.versioning.dto.CreateVersionRequest;
import com.apihub.backend.features.versioning.dto.VersionComparisonDto;
import com.apihub.backend.features.versioning.dto.VersionDto;
import com.apihub.backend.features.versioning.service.VersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VersionController {

    private final VersionService versionService;

    @GetMapping("/apis/{apiId}/versions")
    public ResponseEntity<ApiResponse<List<VersionDto>>> getVersions(@PathVariable Long apiId) {
        List<VersionDto> versions = versionService.getVersionsByApi(apiId);
        return ResponseEntity.ok(ApiResponse.success(versions, "API versions retrieved"));
    }

    @GetMapping("/versions/{versionId}")
    public ResponseEntity<ApiResponse<VersionDto>> getVersionById(@PathVariable Long versionId) {
        VersionDto version = versionService.getVersionById(versionId);
        return ResponseEntity.ok(ApiResponse.success(version, "API version retrieved"));
    }

    @PostMapping("/apis/{apiId}/versions")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<VersionDto>> createDraftVersion(
            @PathVariable Long apiId,
            @Valid @RequestBody CreateVersionRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        VersionDto version = versionService.createDraftVersion(apiId, request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(version, "Draft version created successfully"));
    }

    @PostMapping("/versions/{versionId}/publish")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<VersionDto>> publishVersion(@PathVariable Long versionId) {
        VersionDto version = versionService.publishVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success(version, "Version published as immutable snapshot"));
    }

    @PatchMapping("/versions/{versionId}/lifecycle")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<VersionDto>> updateLifecycle(
            @PathVariable Long versionId,
            @RequestParam String status
    ) {
        VersionDto version = versionService.updateLifecycle(versionId, status);
        return ResponseEntity.ok(ApiResponse.success(version, "Version lifecycle updated to " + status));
    }

    @GetMapping("/versions/compare")
    public ResponseEntity<ApiResponse<VersionComparisonDto>> compareVersions(
            @RequestParam Long fromId,
            @RequestParam Long toId
    ) {
        VersionComparisonDto comparison = versionService.compareVersions(fromId, toId);
        return ResponseEntity.ok(ApiResponse.success(comparison, "Version comparison calculated"));
    }
}
