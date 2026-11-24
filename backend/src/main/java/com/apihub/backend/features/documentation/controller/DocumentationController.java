package com.apihub.backend.features.documentation.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.documentation.dto.CreateEndpointRequest;
import com.apihub.backend.features.documentation.dto.EndpointDto;
import com.apihub.backend.features.documentation.service.DocumentationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class DocumentationController {

    private final DocumentationService documentationService;
    private final com.apihub.backend.features.documentation.service.OpenApiGeneratorService openApiGeneratorService;

    @GetMapping("/versions/{versionId}/openapi")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> getOpenApiSpec(@PathVariable Long versionId) {
        java.util.Map<String, Object> spec = openApiGeneratorService.generateOpenApiSpec(versionId);
        return ResponseEntity.ok(ApiResponse.success(spec, "OpenAPI specification generated"));
    }

    @GetMapping("/versions/{versionId}/endpoints")
    public ResponseEntity<ApiResponse<List<EndpointDto>>> getEndpoints(@PathVariable Long versionId) {
        List<EndpointDto> endpoints = documentationService.getEndpointsByVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success(endpoints, "Endpoints retrieved successfully"));
    }

    @GetMapping("/endpoints/{id}")
    public ResponseEntity<ApiResponse<EndpointDto>> getEndpointById(@PathVariable Long id) {
        EndpointDto endpoint = documentationService.getEndpointById(id);
        return ResponseEntity.ok(ApiResponse.success(endpoint, "Endpoint details retrieved"));
    }

    @PostMapping("/versions/{versionId}/endpoints")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<EndpointDto>> createEndpoint(
            @PathVariable Long versionId,
            @Valid @RequestBody CreateEndpointRequest request
    ) {
        EndpointDto created = documentationService.createEndpoint(versionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Endpoint documented successfully"));
    }
}
