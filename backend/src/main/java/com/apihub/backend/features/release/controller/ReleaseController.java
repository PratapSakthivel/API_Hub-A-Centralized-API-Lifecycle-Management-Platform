package com.apihub.backend.features.release.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.release.dto.CreateReleaseRequest;
import com.apihub.backend.features.release.dto.PromoteReleaseRequest;
import com.apihub.backend.features.release.dto.ReleaseDto;
import com.apihub.backend.features.release.service.ReleaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ReleaseController {

    private final ReleaseService releaseService;

    public ReleaseController(ReleaseService releaseService) {
        this.releaseService = releaseService;
    }

    @GetMapping("/apis/{apiId}/releases")
    public ResponseEntity<ApiResponse<List<ReleaseDto>>> getReleasesForApi(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.getReleasesForApi(apiId)));
    }

    @PostMapping("/releases")
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ReleaseDto>> createRelease(@RequestBody CreateReleaseRequest request) {
        ReleaseDto created = releaseService.createRelease(request);
        return ResponseEntity.status(201).body(ApiResponse.success(created));
    }

    @PostMapping("/releases/{id}/promote")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<ReleaseDto>> promoteRelease(
            @PathVariable Long id,
            @RequestBody PromoteReleaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(releaseService.promoteRelease(id, request)));
    }
}
