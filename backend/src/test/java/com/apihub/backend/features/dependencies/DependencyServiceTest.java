package com.apihub.backend.features.dependencies;

import com.apihub.backend.features.catalog.ApiEntity;
import com.apihub.backend.features.catalog.ApiRepository;
import com.apihub.backend.features.dependencies.dto.CreateDependencyRequest;
import com.apihub.backend.features.dependencies.service.DependencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DependencyServiceTest {

    @Mock
    private ApiDependencyRepository dependencyRepository;

    @Mock
    private ApiRepository apiRepository;

    @InjectMocks
    private DependencyService dependencyService;

    private ApiEntity sourceApi;
    private ApiEntity targetApi;

    @BeforeEach
    void setUp() {
        sourceApi = new ApiEntity();
        sourceApi.setId(1L);
        sourceApi.setName("Payment API");

        targetApi = new ApiEntity();
        targetApi.setId(2L);
        targetApi.setName("Auth API");
    }

    @Test
    void createDependency_Success() {
        // Arrange
        CreateDependencyRequest request = new CreateDependencyRequest();
        request.setSourceApiId(1L);
        request.setTargetApiId(2L);
        request.setRelationshipType("DEPENDS_ON");

        when(apiRepository.findById(1L)).thenReturn(Optional.of(sourceApi));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(targetApi));
        when(dependencyRepository.existsBySourceApiIdAndTargetApiId(1L, 2L)).thenReturn(false);
        when(dependencyRepository.findAllRelatedDependencies(anyLong())).thenReturn(Collections.emptyList());

        ApiDependency saved = new ApiDependency();
        saved.setId(100L);
        saved.setSourceApi(sourceApi);
        saved.setTargetApi(targetApi);
        saved.setRelationshipType("DEPENDS_ON");
        when(dependencyRepository.save(any(ApiDependency.class))).thenReturn(saved);

        // Act
        var result = dependencyService.createDependency(request);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Payment API", result.getSourceApiName());
        assertEquals("Auth API", result.getTargetApiName());
        verify(dependencyRepository, times(1)).save(any(ApiDependency.class));
    }

    @Test
    void createDependency_SelfReference_ThrowsException() {
        // Arrange
        CreateDependencyRequest request = new CreateDependencyRequest();
        request.setSourceApiId(1L);
        request.setTargetApiId(1L);
        request.setRelationshipType("DEPENDS_ON");

        // Act & Assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dependencyService.createDependency(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("self"));
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void createDependency_DuplicateThrowsException() {
        // Arrange
        CreateDependencyRequest request = new CreateDependencyRequest();
        request.setSourceApiId(1L);
        request.setTargetApiId(2L);
        request.setRelationshipType("DEPENDS_ON");

        when(apiRepository.findById(1L)).thenReturn(Optional.of(sourceApi));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(targetApi));
        when(dependencyRepository.existsBySourceApiIdAndTargetApiId(1L, 2L)).thenReturn(true);

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dependencyService.createDependency(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("already exists") ||
                   ex.getMessage().toLowerCase().contains("duplicate"));
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void createDependency_CycleDetected_ThrowsException() {
        // Arrange: 2 → 1 already exists; now trying 1 → 2 would create a cycle
        CreateDependencyRequest request = new CreateDependencyRequest();
        request.setSourceApiId(1L);
        request.setTargetApiId(2L);
        request.setRelationshipType("DEPENDS_ON");

        when(apiRepository.findById(1L)).thenReturn(Optional.of(sourceApi));
        when(apiRepository.findById(2L)).thenReturn(Optional.of(targetApi));
        when(dependencyRepository.existsBySourceApiIdAndTargetApiId(1L, 2L)).thenReturn(false);

        // Simulate existing dependency: 2 → 1
        ApiDependency existing = new ApiDependency();
        existing.setSourceApi(targetApi); // source=2
        existing.setTargetApi(sourceApi); // target=1
        when(dependencyRepository.findAllRelatedDependencies(2L)).thenReturn(List.of(existing));

        // Act & Assert
        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> dependencyService.createDependency(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("cycle") ||
                   ex.getMessage().toLowerCase().contains("circular"));
        verify(dependencyRepository, never()).save(any());
    }

    @Test
    void removeDependency_Success() {
        // Arrange
        ApiDependency dep = new ApiDependency();
        dep.setId(10L);
        dep.setSourceApi(sourceApi);
        dep.setTargetApi(targetApi);
        when(dependencyRepository.findById(10L)).thenReturn(Optional.of(dep));

        // Act
        dependencyService.removeDependency(10L);

        // Assert
        verify(dependencyRepository, times(1)).delete(dep);
    }

    @Test
    void removeDependency_NotFound_ThrowsException() {
        when(dependencyRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> dependencyService.removeDependency(999L)
        );
    }
}
