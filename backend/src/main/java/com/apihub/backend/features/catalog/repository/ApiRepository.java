package com.apihub.backend.features.catalog.repository;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.catalog.domain.LifecycleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiRepository extends JpaRepository<ApiEntity, Long> {
    Optional<ApiEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);

    @Query("SELECT a FROM ApiEntity a WHERE " +
           "(:projectId IS NULL OR a.project.id = :projectId) AND " +
           "(:status IS NULL OR a.lifecycleStatus = :status) AND " +
           "(:query IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.slug) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.summary) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ApiEntity> searchApis(
            @Param("projectId") Long projectId,
            @Param("status") LifecycleStatus status,
            @Param("query") String query,
            Pageable pageable
    );
}
