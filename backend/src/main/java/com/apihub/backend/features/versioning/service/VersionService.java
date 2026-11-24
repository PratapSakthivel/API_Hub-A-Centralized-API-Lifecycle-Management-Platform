package com.apihub.backend.features.versioning.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.dto.EndpointDto;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.dto.CreateVersionRequest;
import com.apihub.backend.features.versioning.dto.VersionComparisonDto;
import com.apihub.backend.features.versioning.dto.VersionDto;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VersionService {

    private final ApiVersionRepository versionRepository;
    private final ApiRepository apiRepository;
    private final ApiEndpointRepository endpointRepository;

    public List<VersionDto> getVersionsByApi(Long apiId) {
        return versionRepository.findByApiIdOrderByCreatedAtDesc(apiId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public VersionDto getVersionById(Long versionId) {
        return toDto(versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId)));
    }

    @Transactional
    public VersionDto createDraftVersion(Long apiId, CreateVersionRequest req, User currentUser) {
        ApiEntity api = apiRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API not found: " + apiId));

        String label = req.getVersionLabel().trim();
        if (versionRepository.existsByApiIdAndVersionLabel(apiId, label)) {
            throw new IllegalArgumentException("Version " + label + " already exists for API: " + api.getName());
        }

        ApiVersion version = ApiVersion.builder()
                .api(api)
                .versionLabel(label)
                .status("DRAFT")
                .changeSummary(req.getChangeSummary())
                .createdBy(currentUser)
                .build();

        return toDto(versionRepository.save(version));
    }

    @Transactional
    public VersionDto publishVersion(Long versionId) {
        ApiVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));

        if ("PUBLISHED".equalsIgnoreCase(version.getStatus())) {
            throw new IllegalStateException("Version is already published.");
        }

        version.setStatus("PUBLISHED");
        version.setPublishedAt(Instant.now());
        return toDto(versionRepository.save(version));
    }

    @Transactional
    public VersionDto updateLifecycle(Long versionId, String status) {
        ApiVersion version = versionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Version not found: " + versionId));

        String upper = status.toUpperCase().trim();
        if (!List.of("DRAFT", "PUBLISHED", "DEPRECATED", "RETIRED").contains(upper)) {
            throw new IllegalArgumentException("Invalid lifecycle status: " + status);
        }

        version.setStatus(upper);
        return toDto(versionRepository.save(version));
    }

    public VersionComparisonDto compareVersions(Long fromId, Long toId) {
        ApiVersion from = versionRepository.findById(fromId)
                .orElseThrow(() -> new IllegalArgumentException("From-version not found: " + fromId));
        ApiVersion to = versionRepository.findById(toId)
                .orElseThrow(() -> new IllegalArgumentException("To-version not found: " + toId));

        List<ApiEndpoint> fromEndpoints = endpointRepository.findByApiVersionIdOrderByPathAsc(fromId);
        List<ApiEndpoint> toEndpoints = endpointRepository.findByApiVersionIdOrderByPathAsc(toId);

        Map<String, ApiEndpoint> fromMap = fromEndpoints.stream()
                .collect(Collectors.toMap(e -> e.getHttpMethod() + ":" + e.getPath(), e -> e));
        Map<String, ApiEndpoint> toMap = toEndpoints.stream()
                .collect(Collectors.toMap(e -> e.getHttpMethod() + ":" + e.getPath(), e -> e));

        List<EndpointDto> added = new ArrayList<>();
        List<EndpointDto> removed = new ArrayList<>();
        List<EndpointDto> modified = new ArrayList<>();

        for (Map.Entry<String, ApiEndpoint> entry : toMap.entrySet()) {
            if (!fromMap.containsKey(entry.getKey())) {
                added.add(toEndpointDto(entry.getValue()));
            } else {
                ApiEndpoint oldEp = fromMap.get(entry.getKey());
                ApiEndpoint newEp = entry.getValue();
                if (!Objects.equals(oldEp.getSummary(), newEp.getSummary()) ||
                    !Objects.equals(oldEp.getDescription(), newEp.getDescription()) ||
                    !Objects.equals(oldEp.getRequestSchema(), newEp.getRequestSchema()) ||
                    !Objects.equals(oldEp.getResponseSchema(), newEp.getResponseSchema())) {
                    modified.add(toEndpointDto(newEp));
                }
            }
        }

        for (Map.Entry<String, ApiEndpoint> entry : fromMap.entrySet()) {
            if (!toMap.containsKey(entry.getKey())) {
                removed.add(toEndpointDto(entry.getValue()));
            }
        }

        return VersionComparisonDto.builder()
                .fromVersionId(from.getId())
                .fromVersionLabel(from.getVersionLabel())
                .toVersionId(to.getId())
                .toVersionLabel(to.getVersionLabel())
                .addedEndpoints(added)
                .removedEndpoints(removed)
                .modifiedEndpoints(modified)
                .build();
    }

    private VersionDto toDto(ApiVersion v) {
        return VersionDto.builder()
                .id(v.getId())
                .apiId(v.getApi().getId())
                .apiName(v.getApi().getName())
                .versionLabel(v.getVersionLabel())
                .status(v.getStatus())
                .changeSummary(v.getChangeSummary())
                .createdById(v.getCreatedBy() != null ? v.getCreatedBy().getId() : null)
                .createdByName(v.getCreatedBy() != null ? v.getCreatedBy().getDisplayName() : null)
                .publishedAt(v.getPublishedAt())
                .createdAt(v.getCreatedAt())
                .updatedAt(v.getUpdatedAt())
                .build();
    }

    private EndpointDto toEndpointDto(ApiEndpoint e) {
        return EndpointDto.builder()
                .id(e.getId())
                .apiVersionId(e.getApiVersion().getId())
                .httpMethod(e.getHttpMethod())
                .path(e.getPath())
                .summary(e.getSummary())
                .description(e.getDescription())
                .operationId(e.getOperationId())
                .requestSchema(e.getRequestSchema())
                .responseSchema(e.getResponseSchema())
                .expectedStatusCode(e.getExpectedStatusCode())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
