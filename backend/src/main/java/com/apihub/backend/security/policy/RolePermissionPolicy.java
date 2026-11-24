package com.apihub.backend.security.policy;

import com.apihub.backend.features.users.domain.RoleName;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

@Component
public class RolePermissionPolicy {

    private final Map<RoleName, Set<PermissionCode>> policy = new EnumMap<>(RoleName.class);

    public RolePermissionPolicy() {
        // Admin has all permissions
        policy.put(RoleName.ADMIN, Set.of(PermissionCode.values()));

        // Developer permissions
        policy.put(RoleName.DEVELOPER, Set.of(
                PermissionCode.PROJECT_CREATE,
                PermissionCode.API_READ,
                PermissionCode.API_CREATE,
                PermissionCode.API_UPDATE,
                PermissionCode.VERSION_CREATE,
                PermissionCode.VERSION_PUBLISH,
                PermissionCode.ENDPOINT_CREATE,
                PermissionCode.ENDPOINT_UPDATE,
                PermissionCode.TEST_READ,
                PermissionCode.TEST_CREATE,
                PermissionCode.TEST_EXECUTE,
                PermissionCode.DEPENDENCY_VIEW,
                PermissionCode.DEPENDENCY_MANAGE,
                PermissionCode.RELEASE_VIEW,
                PermissionCode.RELEASE_CREATE
        ));

        // QA Engineer permissions
        policy.put(RoleName.QA_ENGINEER, Set.of(
                PermissionCode.API_READ,
                PermissionCode.TEST_READ,
                PermissionCode.TEST_CREATE,
                PermissionCode.TEST_EXECUTE,
                PermissionCode.DEPENDENCY_VIEW,
                PermissionCode.RELEASE_VIEW
        ));

        // Project Manager permissions
        policy.put(RoleName.PROJECT_MANAGER, Set.of(
                PermissionCode.PROJECT_CREATE,
                PermissionCode.PROJECT_UPDATE,
                PermissionCode.API_READ,
                PermissionCode.DEPENDENCY_VIEW,
                PermissionCode.RELEASE_VIEW,
                PermissionCode.RELEASE_CREATE,
                PermissionCode.RELEASE_APPROVE,
                PermissionCode.TEST_READ
        ));

        // Consumer permissions (read-only for published catalog & documentation)
        policy.put(RoleName.CONSUMER, Set.of(
                PermissionCode.API_READ,
                PermissionCode.DEPENDENCY_VIEW
        ));
    }

    public boolean hasPermission(RoleName role, PermissionCode permission) {
        Set<PermissionCode> permissions = policy.get(role);
        return permissions != null && permissions.contains(permission);
    }

    public Set<PermissionCode> getPermissionsForRole(RoleName role) {
        return policy.getOrDefault(role, Set.of());
    }
}
