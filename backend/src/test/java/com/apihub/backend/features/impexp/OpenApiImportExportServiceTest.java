package com.apihub.backend.features.impexp;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.documentation.service.OpenApiGeneratorService;
import com.apihub.backend.features.impexp.dto.ImportApiRequest;
import com.apihub.backend.features.impexp.dto.ImportResultDto;
import com.apihub.backend.features.impexp.service.OpenApiImportExportService;
import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenApiImportExportServiceTest {

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ApiVersionRepository versionRepository;

    @Mock
    private ApiEndpointRepository endpointRepository;

    @Mock
    private OpenApiGeneratorService openApiGeneratorService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OpenApiImportExportService service;

    private Project sampleProject;

    @BeforeEach
    void setUp() {
        sampleProject = Project.builder()
                .id(1L)
                .name("Fintech Core")
                .projectKey("FIN")
                .build();
    }

    @Test
    void importOpenApi_ValidJson_CreatesApiVersionAndEndpoints() {
        String validSpec = """
        {
          "openapi": "3.0.1",
          "info": {
            "title": "Payment Gateway API",
            "description": "Processes online credit card transactions",
            "version": "1.2.0"
          },
          "paths": {
            "/payments": {
              "post": {
                "summary": "Create Payment",
                "responses": {
                  "201": { "description": "Created" }
                }
              },
              "get": {
                "summary": "List Payments",
                "responses": {
                  "200": { "description": "OK" }
                }
              }
            }
          }
        }
        """;

        when(projectRepository.findById(1L)).thenReturn(Optional.of(sampleProject));
        when(apiRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        ApiEntity savedApi = ApiEntity.builder()
                .id(10L)
                .name("Payment Gateway API")
                .slug("payment-gateway-api")
                .build();
        when(apiRepository.save(any(ApiEntity.class))).thenReturn(savedApi);

        ApiVersion savedVersion = ApiVersion.builder()
                .id(20L)
                .api(savedApi)
                .versionLabel("v1.2.0")
                .build();
        when(versionRepository.save(any(ApiVersion.class))).thenReturn(savedVersion);

        ImportApiRequest req = ImportApiRequest.builder()
                .projectId(1L)
                .specContent(validSpec)
                .build();

        ImportResultDto result = service.importOpenApi(req);

        assertNotNull(result);
        assertEquals(10L, result.getApiId());
        assertEquals("Payment Gateway API", result.getApiName());
        assertEquals("v1.2.0", result.getVersionLabel());
        assertEquals(2, result.getEndpointsImported());

        verify(apiRepository, times(1)).save(any(ApiEntity.class));
        verify(versionRepository, times(1)).save(any(ApiVersion.class));
        verify(endpointRepository, times(2)).save(any(ApiEndpoint.class));
    }

    @Test
    void importOpenApi_BlankContent_ThrowsException() {
        ImportApiRequest req = ImportApiRequest.builder()
                .projectId(1L)
                .specContent("   ")
                .build();

        assertThrows(IllegalArgumentException.class, () -> service.importOpenApi(req));
        verify(apiRepository, never()).save(any());
    }

    @Test
    void exportOpenApiAsJson_Success() {
        Map<String, Object> mockSpec = Map.of(
                "openapi", "3.0.3",
                "info", Map.of("title", "Exported API", "version", "v1.0.0")
        );
        when(openApiGeneratorService.generateOpenApiSpec(50L)).thenReturn(mockSpec);

        String json = service.exportOpenApiAsJson(50L);

        assertNotNull(json);
        assertTrue(json.contains("Exported API"));
        assertTrue(json.contains("3.0.3"));
    }
}
