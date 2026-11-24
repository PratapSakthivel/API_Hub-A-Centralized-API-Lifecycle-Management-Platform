package com.apihub.backend.features.dependencies.domain;

import com.apihub.backend.features.catalog.domain.ApiEntity;
import com.apihub.backend.features.users.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "api_dependencies", uniqueConstraints = {
    @UniqueConstraint(name = "uk_source_target_api", columnNames = {"source_api_id", "target_api_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiDependency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_api_id", nullable = false)
    private ApiEntity sourceApi; // The API that calls/depends on target

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_api_id", nullable = false)
    private ApiEntity targetApi; // The API being called

    @Column(name = "relationship_type", nullable = false, length = 50)
    @Builder.Default
    private String relationshipType = "REST_CALL"; // REST_CALL, EVENT_SUBSCRIBER, DATA_SYNC

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
