package com.apihub.backend.features.environments.repository;

import com.apihub.backend.features.environments.domain.EnvironmentVariable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvironmentVariableRepository extends JpaRepository<EnvironmentVariable, Long> {
    List<EnvironmentVariable> findByEnvironmentId(Long environmentId);
    Optional<EnvironmentVariable> findByEnvironmentIdAndKey(Long environmentId, String key);
}
