package com.apihub.backend.features.users.dto;

import com.apihub.backend.features.users.domain.RoleName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String displayName;
    private boolean enabled;
    private Set<RoleName> roles;
    private Instant lastLoginAt;
    private Instant createdAt;
    private Instant updatedAt;
}
