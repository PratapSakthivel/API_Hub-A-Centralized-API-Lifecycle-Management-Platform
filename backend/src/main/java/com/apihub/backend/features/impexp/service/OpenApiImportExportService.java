package com.apihub.backend.features.impexp.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.documentation.service.OpenApiGeneratorService;
import com.apihub.backend.features.impexp.dto.ImportApiRequest;
import com.apihub.backend.features.impexp.dto.ImportResultDto;
import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OpenApiImportExportService {

    private final ApiRepository apiRepository;
    private final ProjectRepository projectRepository;
    private final ApiVersionRepository versionRepository;
    private final ApiEndpointRepository endpointRepository;
    private final OpenApiGeneratorService openApiGeneratorService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ImportResultDto importOpenApi(ImportApiRequest request) {
        if (request.getSpecContent() == null || request.getSpecContent().isBlank()) {
            throw new IllegalArgumentException("OpenAPI specification content must not be blank.");
        }

        try {
            JsonNode root = objectMapper.readTree(request.getSpecContent());

            // 1. Resolve Project
            Project project;
            if (request.getProjectId() != null) {
                project = projectRepository.findById(request.getProjectId())
                        .orElseThrow(() -> new IllegalArgumentException("Project not found: " + request.getProjectId()));
            } else {
                project = projectRepository.findAll().stream().findFirst().orElseGet(() -> {
                    Project defaultProj = Project.builder()
                            .name("Default Project")
                            .projectKey("DEFAULT")
                            .description("Auto-generated container for imported APIs")
                            .status("ACTIVE")
                            .build();
                    return projectRepository.save(defaultProj);
                });
            }

            // 2. Extract Info
            JsonNode info = root.path("info");
            String apiName = info.path("title").asText("Imported API");
            String apiDescription = info.path("description").asText("Imported via OpenAPI specification");
            String rawVersion = info.path("version").asText("1.0.0");
            String versionLabel = rawVersion.startsWith("v") ? rawVersion : "v" + rawVersion;

            // Compute Slug
            String baseSlug = (request.getSlug() != null && !request.getSlug().isBlank())
                    ? request.getSlug()
                    : apiName.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (baseSlug.isBlank()) baseSlug = "api";

            String uniqueSlug = baseSlug;
            int counter = 1;
            while (apiRepository.findBySlug(uniqueSlug).isPresent()) {
                uniqueSlug = baseSlug + "-" + counter++;
            }

            // 3. Create ApiEntity
            ApiEntity api = ApiEntity.builder()
                    .project(project)
                    .name(apiName)
                    .slug(uniqueSlug)
                    .summary(apiName)
                    .description(apiDescription)
                    .lifecycleStatus(LifecycleStatus.DRAFT)
                    .build();
            api = apiRepository.save(api);

            // 4. Create ApiVersion
            ApiVersion version = ApiVersion.builder()
                    .api(api)
                    .versionLabel(versionLabel)
                    .status("DRAFT")
                    .changeSummary("Initial imported OpenAPI specification")
                    .createdAt(Instant.now())
                    .build();
            version = versionRepository.save(version);

            // 5. Parse Paths & Endpoints
            JsonNode pathsNode = root.path("paths");
            int endpointCount = 0;

            if (pathsNode.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> pathFields = pathsNode.fields();
                while (pathFields.hasNext()) {
                    Map.Entry<String, JsonNode> pathEntry = pathFields.next();
                    String path = pathEntry.getKey();
                    JsonNode pathItem = pathEntry.getValue();

                    if (pathItem.isObject()) {
                        Iterator<Map.Entry<String, JsonNode>> methodFields = pathItem.fields();
                        while (methodFields.hasNext()) {
                            Map.Entry<String, JsonNode> methodEntry = methodFields.next();
                            String method = methodEntry.getKey().toUpperCase();

                            // Filter valid HTTP methods
                            if (!List.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS").contains(method)) {
                                continue;
                            }

                            JsonNode opNode = methodEntry.getValue();
                            String summary = opNode.path("summary").asText(method + " " + path);
                            String description = opNode.path("description").asText("");
                            String operationId = opNode.path("operationId").asText(null);

                            // Extract expected status code from responses
                            String expectedStatusCode = "200";
                            JsonNode responsesNode = opNode.path("responses");
                            if (responsesNode.isObject() && responsesNode.fieldNames().hasNext()) {
                                String firstCode = responsesNode.fieldNames().next();
                                if (firstCode != null && !firstCode.isBlank() && !"default".equalsIgnoreCase(firstCode)) {
                                    expectedStatusCode = firstCode;
                                }
                            }

                            ApiEndpoint endpoint = ApiEndpoint.builder()
                                    .apiVersion(version)
                                    .httpMethod(method)
                                    .path(path)
                                    .summary(summary)
                                    .description(description)
                                    .operationId(operationId)
                                    .expectedStatusCode(expectedStatusCode)
                                    .build();

                            endpointRepository.save(endpoint);
                            endpointCount++;
                        }
                    }
                }
            }

            return ImportResultDto.builder()
                    .apiId(api.getId())
                    .apiName(api.getName())
                    .slug(api.getSlug())
                    .versionId(version.getId())
                    .versionLabel(version.getVersionLabel())
                    .endpointsImported(endpointCount)
                    .message("OpenAPI specification imported successfully.")
                    .build();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid OpenAPI JSON specification format: " + e.getMessage(), e);
        }
    }

    public String exportOpenApiAsJson(Long versionId) {
        Map<String, Object> spec = openApiGeneratorService.generateOpenApiSpec(versionId);
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OpenAPI specification: " + e.getMessage(), e);
        }
    }
}
