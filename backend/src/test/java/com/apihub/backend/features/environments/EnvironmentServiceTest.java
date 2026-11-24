package com.apihub.backend.features.environments;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.environments.domain.ApiEnvironment;
import com.apihub.backend.features.environments.domain.EnvironmentVariable;
import com.apihub.backend.features.environments.dto.CreateEnvironmentRequest;
import com.apihub.backend.features.environments.dto.EnvironmentDto;
import com.apihub.backend.features.environments.dto.EnvironmentVariableDto;
import com.apihub.backend.features.environments.repository.ApiEnvironmentRepository;
import com.apihub.backend.features.environments.repository.EnvironmentVariableRepository;
import com.apihub.backend.features.environments.service.EnvironmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnvironmentServiceTest {

    @Mock
    private ApiEnvironmentRepository environmentRepository;

    @Mock
    private EnvironmentVariableRepository variableRepository;

    @Mock
    private ApiRepository apiRepository;

    @InjectMocks
    private EnvironmentService environmentService;

    private ApiEntity sampleApi;

    @BeforeEach
    void setUp() {
        sampleApi = ApiEntity.builder()
                .id(1L)
                .name("Core Orders API")
                .slug("core-orders-api")
                .build();
    }

    @Test
    void createEnvironment_SuccessWithVariables() {
        CreateEnvironmentRequest req = CreateEnvironmentRequest.builder()
                .apiId(1L)
                .name("Staging Environment")
                .code("STAGING")
                .baseUrl("https://orders-staging.internal.net")
                .description("Pre-release test cluster")
                .variables(List.of(
                        EnvironmentVariableDto.builder().key("PORT").value("8080").secret(false).build(),
                        EnvironmentVariableDto.builder().key("API_SECRET_KEY").value("sk_live_secret123").secret(true).build()
                ))
                .build();

        when(apiRepository.findById(1L)).thenReturn(Optional.of(sampleApi));
        when(environmentRepository.save(any(ApiEnvironment.class))).thenAnswer(inv -> {
            ApiEnvironment env = inv.getArgument(0);
            env.setId(10L);
            return env;
        });

        EnvironmentDto result = environmentService.createEnvironment(req);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("STAGING", result.getCode());
        assertEquals("https://orders-staging.internal.net", result.getBaseUrl());
        assertEquals(2, result.getVariables().size());

        // Check secret masking
        EnvironmentVariableDto secretVar = result.getVariables().stream()
                .filter(EnvironmentVariableDto::isSecret)
                .findFirst()
                .orElse(null);
        assertNotNull(secretVar);
        assertEquals("••••••••", secretVar.getValue());
    }

    @Test
    void getEnvironments_UnmaskSecrets_ReturnsPlainValues() {
        ApiEnvironment env = ApiEnvironment.builder()
                .id(5L)
                .name("Production")
                .code("PRODUCTION")
                .baseUrl("https://api.internal.net")
                .active(true)
                .variables(new ArrayList<>())
                .build();

        EnvironmentVariable v = EnvironmentVariable.builder()
                .id(100L)
                .environment(env)
                .key("JWT_SECRET")
                .value("super-sensitive-token")
                .secret(true)
                .build();
        env.getVariables().add(v);

        when(environmentRepository.findAll()).thenReturn(List.of(env));

        // When unmasked = true
        List<EnvironmentDto> unmasked = environmentService.getEnvironments(null, true);
        assertEquals("super-sensitive-token", unmasked.get(0).getVariables().get(0).getValue());

        // When unmasked = false
        List<EnvironmentDto> masked = environmentService.getEnvironments(null, false);
        assertEquals("••••••••", masked.get(0).getVariables().get(0).getValue());
    }

    @Test
    void toggleActiveStatus_TogglesFlag() {
        ApiEnvironment env = ApiEnvironment.builder()
                .id(7L)
                .name("Dev")
                .code("DEV")
                .baseUrl("https://dev.local")
                .active(true)
                .variables(new ArrayList<>())
                .build();

        when(environmentRepository.findById(7L)).thenReturn(Optional.of(env));
        when(environmentRepository.save(any(ApiEnvironment.class))).thenAnswer(inv -> inv.getArgument(0));

        EnvironmentDto toggled = environmentService.toggleActiveStatus(7L);
        assertFalse(toggled.isActive());
    }
}
