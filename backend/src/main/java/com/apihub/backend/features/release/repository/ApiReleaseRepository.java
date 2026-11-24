package com.apihub.backend.features.release.repository;

import com.apihub.backend.features.release.domain.ApiRelease;
import com.apihub.backend.features.release.domain.ReleaseStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiReleaseRepository extends JpaRepository<ApiRelease, Long> {
    List<ApiRelease> findByApiIdOrderByCreatedAtDesc(Long apiId);
    Optional<ApiRelease> findTopByApiIdOrderByCreatedAtDesc(Long apiId);
    boolean existsByApiIdAndStage(Long apiId, ReleaseStage stage);
}
