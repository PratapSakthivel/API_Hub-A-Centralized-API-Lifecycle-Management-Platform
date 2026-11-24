package com.apihub.backend.features.dependencies.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.dependencies.domain.ApiDependency;
import com.apihub.backend.features.dependencies.dto.CreateDependencyRequest;
import com.apihub.backend.features.dependencies.dto.DependencyDto;
import com.apihub.backend.features.dependencies.dto.ImpactAnalysisDto;
import com.apihub.backend.features.dependencies.repository.ApiDependencyRepository;
import com.apihub.backend.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DependencyService {

    private final ApiDependencyRepository dependencyRepository;
    private final ApiRepository apiRepository;

    public List<DependencyDto> getDependenciesForApi(Long apiId) {
        return dependencyRepository.findAllRelatedDependencies(apiId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<DependencyDto> getUpstreamDependencies(Long apiId) {
        return dependencyRepository.findByTargetApiId(apiId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<DependencyDto> getDownstreamDependencies(Long apiId) {
        return dependencyRepository.findBySourceApiId(apiId)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public DependencyDto createDependency(CreateDependencyRequest req, User currentUser) {
        if (req.getSourceApiId().equals(req.getTargetApiId())) {
            throw new IllegalArgumentException("An API cannot depend on itself.");
        }

        if (dependencyRepository.existsBySourceApiIdAndTargetApiId(req.getSourceApiId(), req.getTargetApiId())) {
            throw new IllegalArgumentException("Dependency already exists between these APIs.");
        }

        ApiEntity source = apiRepository.findById(req.getSourceApiId())
                .orElseThrow(() -> new IllegalArgumentException("Source API not found: " + req.getSourceApiId()));
        ApiEntity target = apiRepository.findById(req.getTargetApiId())
                .orElseThrow(() -> new IllegalArgumentException("Target API not found: " + req.getTargetApiId()));

        // Cycle detection: check if adding source -> target would create a cycle
        if (wouldCreateCycle(req.getSourceApiId(), req.getTargetApiId())) {
            throw new IllegalStateException("Adding this dependency would create a circular reference in the dependency graph.");
        }

        ApiDependency dep = ApiDependency.builder()
                .sourceApi(source)
                .targetApi(target)
                .relationshipType(req.getRelationshipType() != null ? req.getRelationshipType() : "REST_CALL")
                .notes(req.getNotes())
                .createdBy(currentUser)
                .build();

        return toDto(dependencyRepository.save(dep));
    }

    @Transactional
    public void removeDependency(Long id) {
        if (!dependencyRepository.existsById(id)) {
            throw new IllegalArgumentException("Dependency not found: " + id);
        }
        dependencyRepository.deleteById(id);
    }

    public ImpactAnalysisDto analyzeImpact(Long apiId) {
        ApiEntity api = apiRepository.findById(apiId)
                .orElseThrow(() -> new IllegalArgumentException("API not found: " + apiId));

        List<DependencyDto> upstream = getUpstreamDependencies(apiId);
        List<DependencyDto> downstream = getDownstreamDependencies(apiId);

        // BFS traversal to find all transitively affected downstream APIs
        Set<String> affected = new LinkedHashSet<>();
        Queue<Long> queue = new LinkedList<>();
        Set<Long> visited = new HashSet<>();
        visited.add(apiId);

        for (DependencyDto d : downstream) {
            queue.add(d.getTargetApiId());
        }

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            apiRepository.findById(current).ifPresent(a -> affected.add(a.getName()));

            List<ApiDependency> next = dependencyRepository.findBySourceApiId(current);
            for (ApiDependency n : next) {
                if (!visited.contains(n.getTargetApi().getId())) {
                    queue.add(n.getTargetApi().getId());
                }
            }
        }

        return ImpactAnalysisDto.builder()
                .changedApiId(api.getId())
                .changedApiName(api.getName())
                .depth(affected.size())
                .upstreamDependencies(upstream)
                .downstreamDependencies(downstream)
                .affectedApiNames(new ArrayList<>(affected))
                .cycleDetected(false)
                .build();
    }

    private boolean wouldCreateCycle(Long sourceId, Long targetId) {
        // Check if targetId is already reachable from sourceId (would cause a cycle if we add target->source later)
        // More precisely: check if sourceId is reachable from targetId
        Set<Long> visited = new HashSet<>();
        Queue<Long> queue = new LinkedList<>();
        queue.add(targetId);

        while (!queue.isEmpty()) {
            Long current = queue.poll();
            if (current.equals(sourceId)) return true;
            if (visited.contains(current)) continue;
            visited.add(current);

            dependencyRepository.findBySourceApiId(current)
                    .forEach(d -> queue.add(d.getTargetApi().getId()));
        }
        return false;
    }

    private DependencyDto toDto(ApiDependency d) {
        return DependencyDto.builder()
                .id(d.getId())
                .sourceApiId(d.getSourceApi().getId())
                .sourceApiName(d.getSourceApi().getName())
                .sourceApiSlug(d.getSourceApi().getSlug())
                .targetApiId(d.getTargetApi().getId())
                .targetApiName(d.getTargetApi().getName())
                .targetApiSlug(d.getTargetApi().getSlug())
                .relationshipType(d.getRelationshipType())
                .notes(d.getNotes())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
