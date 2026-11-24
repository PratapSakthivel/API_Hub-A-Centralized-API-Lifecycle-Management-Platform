package com.apihub.backend.features.catalog;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import com.apihub.backend.features.catalog.dto.ApiDto;
import com.apihub.backend.features.catalog.dto.CreateApiRequest;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.catalog.service.ApiCatalogService;
import com.apihub.backend.features.projects.domain.Project;
import com.apihub.backend.features.projects.repository.ProjectRepository;
import com.apihub.backend.features.users.domain.User;
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
class ApiCatalogServiceTest {

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ApiCatalogService apiCatalogService;

    private Project testProject;
    private User testUser;

    @BeforeEach
    void setUp() {
        testProject = Project.builder()
                .id(1L)
                .name("Core Platform")
                .projectKey("CORE")
                .status("ACTIVE")
                .build();

        testUser = User.builder()
                .id(5L)
                .displayName("Developer John")
                .email("john@apihub.local")
                .build();
    }

    @Test
    void createApi_Success() {
        CreateApiRequest req = new CreateApiRequest();
        req.setProjectId(1L);
        req.setName("Payment Service");
        req.setSlug("payment-service");
        req.setSummary("Handles core transaction processing");

        when(apiRepository.existsBySlug("payment-service")).thenReturn(false);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        ApiEntity saved = ApiEntity.builder()
                .id(100L)
                .project(testProject)
                .name("Payment Service")
                .slug("payment-service")
                .summary("Handles core transaction processing")
                .owner(testUser)
                .lifecycleStatus(LifecycleStatus.DRAFT)
                .build();

        when(apiRepository.save(any(ApiEntity.class))).thenReturn(saved);

        ApiDto result = apiCatalogService.createApi(req, testUser);

        assertNotNull(result);
        assertEquals("payment-service", result.getSlug());
        assertEquals(LifecycleStatus.DRAFT, result.getLifecycleStatus());
        verify(apiRepository, times(1)).save(any(ApiEntity.class));
    }

    @Test
    void createApi_DuplicateSlugThrowsException() {
        CreateApiRequest req = new CreateApiRequest();
        req.setProjectId(1L);
        req.setSlug("payment-service");

        when(apiRepository.existsBySlug("payment-service")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> apiCatalogService.createApi(req, testUser));
    }
}
