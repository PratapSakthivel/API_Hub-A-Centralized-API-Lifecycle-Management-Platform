package com.apihub.backend.features.environments.domain;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "api_environments", uniqueConstraints = {
    @UniqueConstraint(name = "uk_api_env_code", columnNames = {"api_id", "code"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiEnvironment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_id")
    private ApiEntity api; // Nullable if organization-wide environment

    @Column(nullable = false, length = 100)
    private String name; // e.g. "Staging Environment"

    @Column(nullable = false, length = 30)
    private String code; // e.g. "DEV", "STAGING", "PRODUCTION", "SANDBOX"

    @Column(nullable = false, length = 255)
    private String baseUrl; // e.g. "https://api-staging.example.com"

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @OneToMany(mappedBy = "environment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EnvironmentVariable> variables = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
