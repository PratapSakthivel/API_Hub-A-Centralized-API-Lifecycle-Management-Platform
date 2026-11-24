package com.apihub.backend.features.environments.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.environments.dto.CreateEnvironmentRequest;
import com.apihub.backend.features.environments.dto.EnvironmentDto;
import com.apihub.backend.features.environments.dto.EnvironmentVariableDto;
import com.apihub.backend.features.environments.service.EnvironmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/environments")
@RequiredArgsConstructor
public class EnvironmentController {

    private final EnvironmentService environmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EnvironmentDto>>> getEnvironments(
            @RequestParam(required = false) Long apiId,
            @RequestParam(defaultValue = "false") boolean unmaskSecrets) {
        return ResponseEntity.ok(ApiResponse.success(environmentService.getEnvironments(apiId, unmaskSecrets)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnvironmentDto>> getEnvironmentById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false") boolean unmaskSecrets) {
        return ResponseEntity.ok(ApiResponse.success(environmentService.getEnvironmentById(id, unmaskSecrets)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<EnvironmentDto>> createEnvironment(@RequestBody CreateEnvironmentRequest request) {
        EnvironmentDto created = environmentService.createEnvironment(request);
        return ResponseEntity.status(201).body(ApiResponse.success(created));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<EnvironmentDto>> toggleActiveStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(environmentService.toggleActiveStatus(id)));
    }

    @PostMapping("/{id}/variables")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<EnvironmentVariableDto>> setVariable(
            @PathVariable Long id,
            @RequestBody EnvironmentVariableDto variableDto) {
        return ResponseEntity.ok(ApiResponse.success(environmentService.setVariable(id, variableDto)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteEnvironment(@PathVariable Long id) {
        environmentService.deleteEnvironment(id);
        return ResponseEntity.ok(ApiResponse.success("Environment deleted successfully."));
    }
}
