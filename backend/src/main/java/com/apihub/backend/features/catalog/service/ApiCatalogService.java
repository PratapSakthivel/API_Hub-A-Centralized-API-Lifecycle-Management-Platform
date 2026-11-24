package com.apihub.backend.features.catalog.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.dto.ApiDto;
import com.apihub.backend.features.catalog.dto.CreateApiRequest;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiCatalogService {

    private final ApiRepository apiRepository;
    private final ProjectRepository projectRepository;

    public Page<ApiDto> searchApis(Long projectId, LifecycleStatus status, String query, Pageable pageable) {
        return apiRepository.searchApis(projectId, status, query, pageable).map(this::toDto);
    }

    public ApiDto getApiById(Long id) {
        return toDto(apiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("API not found with id: " + id)));
    }

    @Transactional
    public ApiDto createApi(CreateApiRequest request, User currentUser) {
        if (apiRepository.existsBySlug(request.getSlug().toLowerCase().trim())) {
            throw new IllegalArgumentException("API slug already exists: " + request.getSlug());
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + request.getProjectId()));

        ApiEntity api = ApiEntity.builder()
                .project(project)
                .name(request.getName().trim())
                .slug(request.getSlug().toLowerCase().trim())
                .summary(request.getSummary())
                .description(request.getDescription())
                .owner(currentUser)
                .lifecycleStatus(request.getLifecycleStatus() != null ? request.getLifecycleStatus() : LifecycleStatus.DRAFT)
                .build();

        return toDto(apiRepository.save(api));
    }

    private ApiDto toDto(ApiEntity entity) {
        return ApiDto.builder()
                .id(entity.getId())
                .projectId(entity.getProject().getId())
                .projectName(entity.getProject().getName())
                .projectKey(entity.getProject().getProjectKey())
                .name(entity.getName())
                .slug(entity.getSlug())
                .summary(entity.getSummary())
                .description(entity.getDescription())
                .ownerId(entity.getOwner() != null ? entity.getOwner().getId() : null)
                .ownerName(entity.getOwner() != null ? entity.getOwner().getDisplayName() : null)
                .lifecycleStatus(entity.getLifecycleStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
