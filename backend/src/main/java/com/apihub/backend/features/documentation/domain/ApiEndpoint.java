package com.apihub.backend.features.documentation.domain;

import com.apihub.backend.features.versioning.domain.ApiVersion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "api_endpoints", uniqueConstraints = {
    @UniqueConstraint(name = "uk_version_path_method", columnNames = {"api_version_id", "path", "http_method"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEndpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_version_id", nullable = false)
    private ApiVersion apiVersion;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod; // GET, POST, PUT, DELETE, PATCH, etc.

    @Column(nullable = false, length = 255)
    private String path; // e.g. "/payments/{id}"

    @Column(nullable = false, length = 150)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String operationId;

    @Column(columnDefinition = "TEXT")
    private String requestSchema; // JSON Schema or OpenAPI parameters

    @Column(columnDefinition = "TEXT")
    private String responseSchema; // JSON Schema or response model

    @Column(length = 10)
    @Builder.Default
    private String expectedStatusCode = "200";

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
