package com.apihub.backend.features.users.dto;

import com.apihub.backend.features.users.domain.RoleName;
import lombok.Data;

import java.util.Set;

@Data
public class UpdateUserRequest {
    private String displayName;
    private Set<RoleName> roles;
    private Boolean enabled;
}
