package com.apihub.backend.features.versioning.repository;

import com.apihub.backend.features.versioning.domain.ApiVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiVersionRepository extends JpaRepository<ApiVersion, Long> {
    List<ApiVersion> findByApiIdOrderByCreatedAtDesc(Long apiId);
    Optional<ApiVersion> findByApiIdAndVersionLabel(Long apiId, String versionLabel);
    boolean existsByApiIdAndVersionLabel(Long apiId, String versionLabel);
}
