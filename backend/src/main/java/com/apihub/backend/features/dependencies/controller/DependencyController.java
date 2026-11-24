package com.apihub.backend.features.dependencies.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.dependencies.dto.CreateDependencyRequest;
import com.apihub.backend.features.dependencies.dto.DependencyDto;
import com.apihub.backend.features.dependencies.dto.ImpactAnalysisDto;
import com.apihub.backend.features.dependencies.service.DependencyService;
import com.apihub.backend.features.users.domain.User;
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
public class DependencyController {

    private final DependencyService dependencyService;

    @GetMapping("/apis/{apiId}/dependencies")
    public ResponseEntity<ApiResponse<List<DependencyDto>>> getDependencies(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(dependencyService.getDependenciesForApi(apiId), "Dependencies retrieved"));
    }

    @GetMapping("/apis/{apiId}/dependencies/upstream")
    public ResponseEntity<ApiResponse<List<DependencyDto>>> getUpstream(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(dependencyService.getUpstreamDependencies(apiId), "Upstream dependencies retrieved"));
    }

    @GetMapping("/apis/{apiId}/dependencies/downstream")
    public ResponseEntity<ApiResponse<List<DependencyDto>>> getDownstream(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(dependencyService.getDownstreamDependencies(apiId), "Downstream dependencies retrieved"));
    }

    @GetMapping("/apis/{apiId}/dependencies/impact")
    public ResponseEntity<ApiResponse<ImpactAnalysisDto>> analyzeImpact(@PathVariable Long apiId) {
        return ResponseEntity.ok(ApiResponse.success(dependencyService.analyzeImpact(apiId), "Impact analysis computed"));
    }

    @PostMapping("/dependencies")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<DependencyDto>> createDependency(
            @Valid @RequestBody CreateDependencyRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        DependencyDto created = dependencyService.createDependency(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Dependency registered"));
    }

    @DeleteMapping("/dependencies/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<Void>> removeDependency(@PathVariable Long id) {
        dependencyService.removeDependency(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Dependency removed"));
    }
}
