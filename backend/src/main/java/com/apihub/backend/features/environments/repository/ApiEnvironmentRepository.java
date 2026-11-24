package com.apihub.backend.features.environments.repository;

import com.apihub.backend.features.environments.domain.ApiEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiEnvironmentRepository extends JpaRepository<ApiEnvironment, Long> {
    List<ApiEnvironment> findByApiId(Long apiId);
    List<ApiEnvironment> findByApiIsNull();
    Optional<ApiEnvironment> findByApiIdAndCode(Long apiId, String code);
    Optional<ApiEnvironment> findByApiIsNullAndCode(String code);
}
