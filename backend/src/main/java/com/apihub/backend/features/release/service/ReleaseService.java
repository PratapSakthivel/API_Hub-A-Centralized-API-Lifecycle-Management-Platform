package com.apihub.backend.features.release.service;

import com.apihub.backend.features.catalog.ApiEntity;
import com.apihub.backend.features.catalog.ApiRepository;
import com.apihub.backend.features.release.domain.ApiRelease;
import com.apihub.backend.features.release.domain.ReleaseStage;
import com.apihub.backend.features.release.dto.CreateReleaseRequest;
import com.apihub.backend.features.release.dto.PromoteReleaseRequest;
import com.apihub.backend.features.release.dto.ReleaseDto;
import com.apihub.backend.features.release.repository.ApiReleaseRepository;
import com.apihub.backend.features.versioning.ApiVersion;
import com.apihub.backend.features.versioning.ApiVersionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class ReleaseService {

    private final ApiReleaseRepository releaseRepository;
    private final ApiRepository apiRepository;
    private final ApiVersionRepository versionRepository;

    public ReleaseService(ApiReleaseRepository releaseRepository,
                          ApiRepository apiRepository,
                          ApiVersionRepository versionRepository) {
        this.releaseRepository = releaseRepository;
        this.apiRepository = apiRepository;
        this.versionRepository = versionRepository;
    }

    public List<ReleaseDto> getReleasesForApi(Long apiId) {
        return releaseRepository.findByApiIdOrderByCreatedAtDesc(apiId)
                .stream().map(this::toDto).toList();
    }

    public ReleaseDto createRelease(CreateReleaseRequest request) {
        ApiEntity api = apiRepository.findById(request.getApiId())
                .orElseThrow(() -> new EntityNotFoundException("API not found: " + request.getApiId()));

        ApiRelease release = new ApiRelease();
        release.setApi(api);
        release.setStage(ReleaseStage.DEVELOPMENT);
        release.setNotes(request.getNotes());

        if (request.getVersionId() != null) {
            ApiVersion version = versionRepository.findById(request.getVersionId())
                    .orElseThrow(() -> new EntityNotFoundException("Version not found: " + request.getVersionId()));
            release.setVersion(version);
        }

        return toDto(releaseRepository.save(release));
    }

    public ReleaseDto promoteRelease(Long releaseId, PromoteReleaseRequest request) {
        ApiRelease release = releaseRepository.findById(releaseId)
                .orElseThrow(() -> new EntityNotFoundException("Release not found: " + releaseId));

        ReleaseStage currentStage = release.getStage();
        ReleaseStage expectedNext = currentStage.next();

        if (request.getTargetStage() != null && request.getTargetStage() != expectedNext) {
            throw new IllegalArgumentException(
                    "Invalid promotion: must go from " + currentStage + " → " + expectedNext +
                    ", but requested " + request.getTargetStage());
        }

        release.setStage(expectedNext);
        release.setApprovedBy(request.getApprovedBy());
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            release.setNotes(request.getNotes());
        }
        release.setPromotedAt(LocalDateTime.now());

        return toDto(releaseRepository.save(release));
    }

    private ReleaseDto toDto(ApiRelease r) {
        ReleaseDto dto = new ReleaseDto();
        dto.setId(r.getId());
        dto.setApiId(r.getApi().getId());
        dto.setApiName(r.getApi().getName());
        dto.setStage(r.getStage());
        dto.setApprovedBy(r.getApprovedBy());
        dto.setNotes(r.getNotes());
        dto.setPromotedAt(r.getPromotedAt());
        dto.setCreatedAt(r.getCreatedAt());
        if (r.getVersion() != null) {
            dto.setVersionId(r.getVersion().getId());
            dto.setVersionLabel(r.getVersion().getVersionNumber());
        }
        return dto;
    }
}
