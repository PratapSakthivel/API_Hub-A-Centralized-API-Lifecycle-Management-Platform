package com.apihub.backend.features.catalog.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.dto.ApiDto;
import com.apihub.backend.features.catalog.dto.CreateApiRequest;
import com.apihub.backend.features.catalog.service.ApiCatalogService;
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

@RestController
@RequestMapping("/api/v1/apis")
@RequiredArgsConstructor
public class ApiCatalogController {

    private final ApiCatalogService apiCatalogService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ApiDto>>> getApis(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) LifecycleStatus status,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ApiDto> results = apiCatalogService.searchApis(projectId, status, q, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Catalog APIs retrieved"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ApiDto>> getApiById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(apiCatalogService.getApiById(id), "API details retrieved"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    public ResponseEntity<ApiResponse<ApiDto>> createApi(
            @Valid @RequestBody CreateApiRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        ApiDto created = apiCatalogService.createApi(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "API registered successfully"));
    }
}
