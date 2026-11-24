package com.apihub.backend.features.impexp.controller;

import com.apihub.backend.common.dto.ApiResponse;
import com.apihub.backend.features.impexp.dto.ImportApiRequest;
import com.apihub.backend.features.impexp.dto.ImportResultDto;
import com.apihub.backend.features.impexp.service.OpenApiImportExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ApiImportExportController {

    private final OpenApiImportExportService importExportService;

    @PostMapping("/apis/import")
    @PreAuthorize("hasAnyRole('DEVELOPER', 'ADMIN', 'PROJECT_OWNER')")
    public ResponseEntity<ApiResponse<ImportResultDto>> importApi(@RequestBody ImportApiRequest request) {
        ImportResultDto result = importExportService.importOpenApi(request);
        return ResponseEntity.status(201).body(ApiResponse.success(result));
    }

    @GetMapping(value = "/versions/{versionId}/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> exportSpecificationDownload(@PathVariable Long versionId) {
        String jsonSpec = importExportService.exportOpenApiAsJson(versionId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"openapi-version-" + versionId + ".json\"")
                .body(jsonSpec);
    }

    @GetMapping("/versions/{versionId}/export/preview")
    public ResponseEntity<ApiResponse<Map<String, String>>> exportSpecificationPreview(@PathVariable Long versionId) {
        String jsonSpec = importExportService.exportOpenApiAsJson(versionId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("spec", jsonSpec)));
    }
}
