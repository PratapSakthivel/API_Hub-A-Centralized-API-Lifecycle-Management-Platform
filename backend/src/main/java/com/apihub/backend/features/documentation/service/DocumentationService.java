package com.apihub.backend.features.documentation.service;

import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.dto.CreateEndpointRequest;
import com.apihub.backend.features.documentation.dto.EndpointDto;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentationService {

    private final ApiEndpointRepository endpointRepository;
    private final ApiVersionRepository versionRepository;

    public List<EndpointDto> getEndpointsByVersion(Long versionId) {
        return endpointRepository.findByApiVersionIdOrderByPathAsc(versionId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public EndpointDto getEndpointById(Long id) {
        return toDto(endpointRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Endpoint not found with id: " + id)));
    }

    @Transactional
    public EndpointDto createEndpoint(Long versionId, CreateEndpointRequest req) {
        ApiVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("API version not found: " + versionId));

        if ("PUBLISHED".equalsIgnoreCase(version.getStatus())) {
            throw new IllegalStateException("Cannot modify endpoints of an immutable PUBLISHED API version. Create a new draft version.");
        }

        String normalizedMethod = req.getHttpMethod().toUpperCase().trim();
        String normalizedPath = req.getPath().startsWith("/") ? req.getPath().trim() : "/" + req.getPath().trim();

        if (endpointRepository.existsByApiVersionIdAndPathAndHttpMethod(versionId, normalizedPath, normalizedMethod)) {
            throw new IllegalArgumentException("Endpoint with method " + normalizedMethod + " and path " + normalizedPath + " already exists in this version.");
        }

        ApiEndpoint endpoint = ApiEndpoint.builder()
                .apiVersion(version)
                .httpMethod(normalizedMethod)
                .path(normalizedPath)
                .summary(req.getSummary().trim())
                .description(req.getDescription())
                .operationId(req.getOperationId())
                .requestSchema(req.getRequestSchema())
                .responseSchema(req.getResponseSchema())
                .expectedStatusCode(req.getExpectedStatusCode() != null ? req.getExpectedStatusCode() : "200")
                .build();

        return toDto(endpointRepository.save(endpoint));
    }

    private EndpointDto toDto(ApiEndpoint entity) {
        return EndpointDto.builder()
                .id(entity.getId())
                .apiVersionId(entity.getApiVersion().getId())
                .httpMethod(entity.getHttpMethod())
                .path(entity.getPath())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .operationId(entity.getOperationId())
                .requestSchema(entity.getRequestSchema())
                .responseSchema(entity.getResponseSchema())
                .expectedStatusCode(entity.getExpectedStatusCode())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
