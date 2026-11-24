package com.apihub.backend.features.testing.repository;

import com.apihub.backend.features.testing.domain.TestRun;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    Page<TestRun> findByApiIdOrderByStartedAtDesc(Long apiId, Pageable pageable);
}
