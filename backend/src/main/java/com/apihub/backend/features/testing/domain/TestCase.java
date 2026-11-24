package com.apihub.backend.features.testing.domain;

import com.apihub.backend.features.versioning.domain.ApiVersion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "test_cases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_suite_id", nullable = false)
    private TestSuite testSuite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_version_id")
    private ApiVersion apiVersion;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE, etc.

    @Column(name = "endpoint_url", nullable = false, length = 500)
    private String endpointUrl; // Target URL or path

    @Column(columnDefinition = "TEXT")
    private String headersJson; // JSON map of headers

    @Column(columnDefinition = "TEXT")
    private String requestBody;

    @Column(name = "expected_status_code", nullable = false)
    @Builder.Default
    private Integer expectedStatusCode = 200;

    @Builder.Default
    @Column(nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
