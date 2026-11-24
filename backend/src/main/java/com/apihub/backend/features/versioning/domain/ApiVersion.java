package com.apihub.backend.features.versioning.domain;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.users.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "api_versions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_api_version_label", columnNames = {"api_id", "version_label"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "api_id", nullable = false)
    private ApiEntity api;

    @Column(name = "version_label", nullable = false, length = 30)
    private String versionLabel; // e.g. "v1.0.0"

    @Column(length = 30, nullable = false)
    @Builder.Default
    private String status = "DRAFT"; // DRAFT, PUBLISHED, DEPRECATED, RETIRED

    @Column(columnDefinition = "TEXT")
    private String changeSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    private Instant publishedAt;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
