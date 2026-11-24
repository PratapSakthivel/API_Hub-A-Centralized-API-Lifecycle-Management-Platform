package com.apihub.backend.features.documentation.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenApiGeneratorService {

    private final ApiVersionRepository versionRepository;
    private final ApiEndpointRepository endpointRepository;

    public Map<String, Object> generateOpenApiSpec(Long versionId) {
        ApiVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));

        ApiEntity api = version.getApi();
        List<ApiEndpoint> endpoints = endpointRepository.findByApiVersionIdOrderByPathAsc(versionId);

        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("openapi", "3.0.3");

        // Info object
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", api.getName());
        info.put("description", api.getDescription() != null ? api.getDescription() : api.getSummary());
        info.put("version", version.getVersionLabel());
        spec.put("info", info);

        // Paths map
        Map<String, Object> paths = new LinkedHashMap<>();
        for (ApiEndpoint ep : endpoints) {
            Map<String, Object> pathItem = (Map<String, Object>) paths.computeIfAbsent(ep.getPath(), k -> new LinkedHashMap<>());

            Map<String, Object> operation = new LinkedHashMap<>();
            operation.put("summary", ep.getSummary());
            if (ep.getDescription() != null) {
                operation.put("description", ep.getDescription());
            }
            if (ep.getOperationId() != null) {
                operation.put("operationId", ep.getOperationId());
            }

            // Responses
            Map<String, Object> responses = new LinkedHashMap<>();
            Map<String, Object> defaultResponse = new LinkedHashMap<>();
            defaultResponse.put("description", "Successful operation");
            responses.put(ep.getExpectedStatusCode() != null ? ep.getExpectedStatusCode() : "200", defaultResponse);
            operation.put("responses", responses);

            pathItem.put(ep.getHttpMethod().toLowerCase(), operation);
        }

        spec.put("paths", paths);
        return spec;
    }
}
