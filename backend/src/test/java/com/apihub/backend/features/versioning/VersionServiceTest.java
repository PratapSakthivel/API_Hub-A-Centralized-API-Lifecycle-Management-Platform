package com.apihub.backend.features.versioning;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.documentation.repository.ApiEndpointRepository;
import com.apihub.backend.features.users.domain.User;
import com.apihub.backend.features.versioning.domain.ApiVersion;
import com.apihub.backend.features.versioning.dto.CreateVersionRequest;
import com.apihub.backend.features.versioning.dto.VersionDto;
import com.apihub.backend.features.versioning.repository.ApiVersionRepository;
import com.apihub.backend.features.versioning.service.VersionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VersionServiceTest {

    @Mock
    private ApiVersionRepository versionRepository;

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ApiEndpointRepository endpointRepository;

    @InjectMocks
    private VersionService versionService;

    private ApiEntity testApi;
    private User testUser;

    @BeforeEach
    void setUp() {
        testApi = ApiEntity.builder().id(1L).name("Order Service").build();
        testUser = User.builder().id(2L).displayName("Dev Alice").build();
    }

    @Test
    void createDraftVersion_Success() {
        CreateVersionRequest req = new CreateVersionRequest();
        req.setVersionLabel("v1.0.0");
        req.setChangeSummary("Initial release");

        when(apiRepository.findById(1L)).thenReturn(Optional.of(testApi));
        when(versionRepository.existsByApiIdAndVersionLabel(1L, "v1.0.0")).thenReturn(false);

        ApiVersion saved = ApiVersion.builder()
                .id(10L)
                .api(testApi)
                .versionLabel("v1.0.0")
                .status("DRAFT")
                .changeSummary("Initial release")
                .createdBy(testUser)
                .build();

        when(versionRepository.save(any(ApiVersion.class))).thenReturn(saved);

        VersionDto dto = versionService.createDraftVersion(1L, req, testUser);

        assertNotNull(dto);
        assertEquals("v1.0.0", dto.getVersionLabel());
        assertEquals("DRAFT", dto.getStatus());
        verify(versionRepository, times(1)).save(any(ApiVersion.class));
    }

    @Test
    void publishVersion_Success() {
        ApiVersion draft = ApiVersion.builder()
                .id(10L)
                .api(testApi)
                .versionLabel("v1.0.0")
                .status("DRAFT")
                .build();

        when(versionRepository.findById(10L)).thenReturn(Optional.of(draft));
        when(versionRepository.save(any(ApiVersion.class))).thenAnswer(i -> i.getArgument(0));

        VersionDto published = versionService.publishVersion(10L);

        assertEquals("PUBLISHED", published.getStatus());
        assertNotNull(published.getPublishedAt());
    }

    @Test
    void updateLifecycle_DeprecateSuccess() {
        ApiVersion published = ApiVersion.builder()
                .id(10L)
                .api(testApi)
                .versionLabel("v1.0.0")
                .status("PUBLISHED")
                .build();

        when(versionRepository.findById(10L)).thenReturn(Optional.of(published));
        when(versionRepository.save(any(ApiVersion.class))).thenAnswer(i -> i.getArgument(0));

        VersionDto deprecated = versionService.updateLifecycle(10L, "DEPRECATED");
        assertEquals("DEPRECATED", deprecated.getStatus());
    }
}
