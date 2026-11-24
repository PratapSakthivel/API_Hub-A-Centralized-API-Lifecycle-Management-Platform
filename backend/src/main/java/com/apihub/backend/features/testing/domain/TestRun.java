package com.apihub.backend.features.testing.domain;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.users.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiEntity api;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_suite_id")
    private TestSuite testSuite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "triggered_by_user_id")
    private User triggeredBy;

    @Column(name = "overall_status", nullable = false, length = 20)
    private String overallStatus; // PASSED, FAILED, RUNNING

    private int totalTests;
    private int passedTests;
    private int failedTests;

    private Long durationMs;

    @Builder.Default
    @OneToMany(mappedBy = "testRun", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TestResult> results = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant startedAt;

    private Instant completedAt;
}
