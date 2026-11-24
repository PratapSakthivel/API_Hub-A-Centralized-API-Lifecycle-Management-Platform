package com.apihub.backend.features.testing.repository;

import com.apihub.backend.features.testing.domain.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    List<TestSuite> findByApiId(Long apiId);
}
