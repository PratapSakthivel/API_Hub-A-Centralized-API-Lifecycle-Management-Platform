package com.apihub.backend.features.testing.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "test_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_run_id", nullable = false)
    private TestRun testRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_case_id")
    private TestCase testCase;

    @Column(nullable = false, length = 150)
    private String testCaseName;

    private Integer expectedStatus;
    private Integer actualStatus;
    private Long durationMs;

    @Column(nullable = false, length = 20)
    private String status; // PASSED, FAILED, ERROR

    @Column(columnDefinition = "TEXT")
    private String sanitizedResponse;

    @Column(columnDefinition = "TEXT")
    private String failureMessage;
}
