package com.apihub.backend.features.environments.service;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.repository.ApiRepository;
import com.apihub.backend.features.environments.domain.ApiEnvironment;
import com.apihub.backend.features.environments.domain.EnvironmentVariable;
import com.apihub.backend.features.environments.dto.CreateEnvironmentRequest;
import com.apihub.backend.features.environments.dto.EnvironmentDto;
import com.apihub.backend.features.environments.dto.EnvironmentVariableDto;
import com.apihub.backend.features.environments.repository.ApiEnvironmentRepository;
import com.apihub.backend.features.environments.repository.EnvironmentVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnvironmentService {

    public static final String MASKED_VALUE = "••••••••";

    private final ApiEnvironmentRepository environmentRepository;
    private final EnvironmentVariableRepository variableRepository;
    private final ApiRepository apiRepository;

    @Transactional(readOnly = true)
    public List<EnvironmentDto> getEnvironments(Long apiId, boolean unmaskSecrets) {
        List<ApiEnvironment> envs = apiId != null
                ? environmentRepository.findByApiId(apiId)
                : environmentRepository.findAll();

        return envs.stream()
                .map(env -> toDto(env, unmaskSecrets))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnvironmentDto getEnvironmentById(Long id, boolean unmaskSecrets) {
        ApiEnvironment env = environmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + id));
        return toDto(env, unmaskSecrets);
    }

    @Transactional
    public EnvironmentDto createEnvironment(CreateEnvironmentRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Environment name must not be blank.");
        }
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new IllegalArgumentException("Environment code must not be blank.");
        }
        if (request.getBaseUrl() == null || request.getBaseUrl().isBlank()) {
            throw new IllegalArgumentException("Base URL must not be blank.");
        }

        ApiEntity api = null;
        if (request.getApiId() != null) {
            api = apiRepository.findById(request.getApiId())
                    .orElseThrow(() -> new IllegalArgumentException("API not found: " + request.getApiId()));
        }

        ApiEnvironment env = ApiEnvironment.builder()
                .api(api)
                .name(request.getName().trim())
                .code(request.getCode().trim().toUpperCase())
                .baseUrl(request.getBaseUrl().trim())
                .description(request.getDescription())
                .active(true)
                .variables(new ArrayList<>())
                .build();

        if (request.getVariables() != null) {
            for (EnvironmentVariableDto varDto : request.getVariables()) {
                if (varDto.getKey() != null && !varDto.getKey().isBlank()) {
                    EnvironmentVariable v = EnvironmentVariable.builder()
                            .environment(env)
                            .key(varDto.getKey().trim())
                            .value(varDto.getValue() != null ? varDto.getValue() : "")
                            .secret(varDto.isSecret())
                            .build();
                    env.getVariables().add(v);
                }
            }
        }

        ApiEnvironment saved = environmentRepository.save(env);
        return toDto(saved, false);
    }

    @Transactional
    public EnvironmentDto toggleActiveStatus(Long id) {
        ApiEnvironment env = environmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + id));
        env.setActive(!env.isActive());
        return toDto(environmentRepository.save(env), false);
    }

    @Transactional
    public EnvironmentVariableDto setVariable(Long environmentId, EnvironmentVariableDto dto) {
        ApiEnvironment env = environmentRepository.findById(environmentId)
                .orElseThrow(() -> new IllegalArgumentException("Environment not found: " + environmentId));

        EnvironmentVariable variable = variableRepository
                .findByEnvironmentIdAndKey(environmentId, dto.getKey())
                .orElseGet(() -> EnvironmentVariable.builder()
                        .environment(env)
                        .key(dto.getKey().trim())
                        .build());

        variable.setValue(dto.getValue() != null ? dto.getValue() : "");
        variable.setSecret(dto.isSecret());

        EnvironmentVariable saved = variableRepository.save(variable);
        return EnvironmentVariableDto.builder()
                .id(saved.getId())
                .key(saved.getKey())
                .value(saved.isSecret() ? MASKED_VALUE : saved.getValue())
                .secret(saved.isSecret())
                .build();
    }

    @Transactional
    public void deleteEnvironment(Long id) {
        if (!environmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Environment not found: " + id);
        }
        environmentRepository.deleteById(id);
    }

    private EnvironmentDto toDto(ApiEnvironment env, boolean unmaskSecrets) {
        List<EnvironmentVariableDto> varDtos = env.getVariables().stream()
                .map(v -> EnvironmentVariableDto.builder()
                        .id(v.getId())
                        .key(v.getKey())
                        .value(v.isSecret() && !unmaskSecrets ? MASKED_VALUE : v.getValue())
                        .secret(v.isSecret())
                        .build())
                .collect(Collectors.toList());

        return EnvironmentDto.builder()
                .id(env.getId())
                .apiId(env.getApi() != null ? env.getApi().getId() : null)
                .apiName(env.getApi() != null ? env.getApi().getName() : "Global")
                .name(env.getName())
                .code(env.getCode())
                .baseUrl(env.getBaseUrl())
                .description(env.getDescription())
                .active(env.isActive())
                .variables(varDtos)
                .createdAt(env.getCreatedAt())
                .build();
    }
}
