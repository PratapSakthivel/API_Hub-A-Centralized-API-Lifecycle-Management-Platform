package com.apihub.backend.features.testing.repository;

import com.apihub.backend.features.testing.domain.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    List<TestCase> findByTestSuiteId(Long testSuiteId);
}
