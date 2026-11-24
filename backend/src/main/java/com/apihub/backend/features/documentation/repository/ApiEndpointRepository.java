package com.apihub.backend.features.documentation.repository;

import com.apihub.backend.features.documentation.domain.ApiEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiEndpointRepository extends JpaRepository<ApiEndpoint, Long> {
    List<ApiEndpoint> findByApiVersionIdOrderByPathAsc(Long apiVersionId);
    Optional<ApiEndpoint> findByApiVersionIdAndPathAndHttpMethod(Long apiVersionId, String path, String httpMethod);
    boolean existsByApiVersionIdAndPathAndHttpMethod(Long apiVersionId, String path, String httpMethod);
}
