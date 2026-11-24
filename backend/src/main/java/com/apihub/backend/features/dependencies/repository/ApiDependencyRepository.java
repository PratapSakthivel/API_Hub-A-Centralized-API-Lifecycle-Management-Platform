package com.apihub.backend.features.dependencies.repository;

import com.apihub.backend.features.dependencies.domain.ApiDependency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiDependencyRepository extends JpaRepository<ApiDependency, Long> {

    List<ApiDependency> findBySourceApiId(Long sourceApiId);

    List<ApiDependency> findByTargetApiId(Long targetApiId);

    boolean existsBySourceApiIdAndTargetApiId(Long sourceApiId, Long targetApiId);

    @Query("SELECT d FROM ApiDependency d WHERE d.sourceApi.id = :apiId OR d.targetApi.id = :apiId")
    List<ApiDependency> findAllRelatedDependencies(@Param("apiId") Long apiId);
}
