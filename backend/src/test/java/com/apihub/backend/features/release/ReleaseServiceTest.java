package com.apihub.backend.features.release;

import com.apihub.backend.features.catalog.ApiEntity;
import com.apihub.backend.features.catalog.ApiRepository;
import com.apihub.backend.features.release.domain.ApiRelease;
import com.apihub.backend.features.release.domain.ReleaseStage;
import com.apihub.backend.features.release.dto.CreateReleaseRequest;
import com.apihub.backend.features.release.dto.PromoteReleaseRequest;
import com.apihub.backend.features.release.dto.ReleaseDto;
import com.apihub.backend.features.release.repository.ApiReleaseRepository;
import com.apihub.backend.features.release.service.ReleaseService;
import com.apihub.backend.features.versioning.ApiVersionRepository;
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
class ReleaseServiceTest {

    @Mock
    private ApiReleaseRepository releaseRepository;

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ApiVersionRepository versionRepository;

    @InjectMocks
    private ReleaseService releaseService;

    private ApiEntity api;

    @BeforeEach
    void setUp() {
        api = new ApiEntity();
        api.setId(1L);
        api.setName("Payment API");
    }

    @Test
    void createRelease_StartsInDevelopment() {
        CreateReleaseRequest request = new CreateReleaseRequest();
        request.setApiId(1L);
        request.setNotes("Initial release");

        when(apiRepository.findById(1L)).thenReturn(Optional.of(api));

        ApiRelease saved = new ApiRelease();
        saved.setId(10L);
        saved.setApi(api);
        saved.setStage(ReleaseStage.DEVELOPMENT);
        saved.setNotes("Initial release");
        when(releaseRepository.save(any(ApiRelease.class))).thenReturn(saved);

        ReleaseDto result = releaseService.createRelease(request);

        assertNotNull(result);
        assertEquals(ReleaseStage.DEVELOPMENT, result.getStage());
        assertEquals("Payment API", result.getApiName());
    }

    @Test
    void promoteRelease_DevToTesting_Success() {
        ApiRelease release = new ApiRelease();
        release.setId(10L);
        release.setApi(api);
        release.setStage(ReleaseStage.DEVELOPMENT);

        when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));
        when(releaseRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PromoteReleaseRequest req = new PromoteReleaseRequest();
        req.setTargetStage(ReleaseStage.TESTING);
        req.setApprovedBy("Admin");

        ReleaseDto result = releaseService.promoteRelease(10L, req);

        assertEquals(ReleaseStage.TESTING, result.getStage());
        assertEquals("Admin", result.getApprovedBy());
    }

    @Test
    void promoteRelease_SkipStage_ThrowsException() {
        ApiRelease release = new ApiRelease();
        release.setId(10L);
        release.setApi(api);
        release.setStage(ReleaseStage.DEVELOPMENT);

        when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

        PromoteReleaseRequest req = new PromoteReleaseRequest();
        req.setTargetStage(ReleaseStage.PRODUCTION); // Skip 2 stages

        assertThrows(IllegalArgumentException.class, () -> releaseService.promoteRelease(10L, req));
    }

    @Test
    void promoteRelease_AlreadyProduction_ThrowsException() {
        ApiRelease release = new ApiRelease();
        release.setId(10L);
        release.setApi(api);
        release.setStage(ReleaseStage.PRODUCTION);

        when(releaseRepository.findById(10L)).thenReturn(Optional.of(release));

        PromoteReleaseRequest req = new PromoteReleaseRequest();

        assertThrows(IllegalStateException.class, () -> releaseService.promoteRelease(10L, req));
    }

    @Test
    void createRelease_ApiNotFound_ThrowsException() {
        CreateReleaseRequest request = new CreateReleaseRequest();
        request.setApiId(999L);

        when(apiRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> releaseService.createRelease(request));
    }
}
