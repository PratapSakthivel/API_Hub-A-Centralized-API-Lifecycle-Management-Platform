package com.apihub.backend.features.environments.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "environment_variables", uniqueConstraints = {
    @UniqueConstraint(name = "uk_env_var_key", columnNames = {"environment_id", "var_key"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentVariable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "environment_id", nullable = false)
    private ApiEnvironment environment;

    @Column(name = "var_key", nullable = false, length = 100)
    private String key;

    @Column(name = "var_value", columnDefinition = "TEXT", nullable = false)
    private String value;

    @Builder.Default
    @Column(nullable = false)
    private boolean secret = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
