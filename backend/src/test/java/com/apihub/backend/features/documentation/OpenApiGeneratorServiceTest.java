package com.apihub.backend.features.documentation;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.documentation.service.OpenApiGeneratorService;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenApiGeneratorServiceTest {

    @Mock
    private ApiVersionRepository versionRepository;

    @Mock
    private ApiEndpointRepository endpointRepository;

    @InjectMocks
    private OpenApiGeneratorService openApiGeneratorService;

    @Test
    void generateOpenApiSpec_Success() {
        ApiEntity api = ApiEntity.builder()
                .id(1L)
                .name("Payments API")
                .summary("Payment processing API")
                .build();

        ApiVersion version = ApiVersion.builder()
                .id(10L)
                .api(api)
                .versionLabel("v1.0.0")
                .status("DRAFT")
                .build();

        ApiEndpoint endpoint = ApiEndpoint.builder()
                .id(100L)
                .apiVersion(version)
                .httpMethod("POST")
                .path("/payments")
                .summary("Process payment")
                .expectedStatusCode("201")
                .build();

        when(versionRepository.findById(10L)).thenReturn(Optional.of(version));
        when(endpointRepository.findByApiVersionIdOrderByPathAsc(10L)).thenReturn(List.of(endpoint));

        Map<String, Object> spec = openApiGeneratorService.generateOpenApiSpec(10L);

        assertNotNull(spec);
        assertEquals("3.0.3", spec.get("openapi"));

        Map<String, Object> info = (Map<String, Object>) spec.get("info");
        assertEquals("Payments API", info.get("title"));
        assertEquals("v1.0.0", info.get("version"));

        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        assertTrue(paths.containsKey("/payments"));
    }
}
