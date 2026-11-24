package com.apihub.backend.features.projects.service;

import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.dto.ProjectDto;
import com.apihub.backend.features.projects.dto.ProjectRequest;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public ProjectDto getProjectById(Long id) {
        return toDto(projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + id)));
    }

    @Transactional
    public ProjectDto createProject(ProjectRequest request) {
        if (projectRepository.existsByProjectKey(request.getProjectKey().toUpperCase().trim())) {
            throw new IllegalArgumentException("Project key already exists: " + request.getProjectKey());
        }

        Project project = Project.builder()
                .name(request.getName().trim())
                .projectKey(request.getProjectKey().toUpperCase().trim())
                .description(request.getDescription())
                .status("ACTIVE")
                .build();

        return toDto(projectRepository.save(project));
    }

    private ProjectDto toDto(Project project) {
        return ProjectDto.builder()
                .id(project.getId())
                .name(project.getName())
                .projectKey(project.getProjectKey())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
