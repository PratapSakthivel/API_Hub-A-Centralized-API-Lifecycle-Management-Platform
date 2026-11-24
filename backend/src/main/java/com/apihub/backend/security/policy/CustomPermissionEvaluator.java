package com.apihub.backend.security.policy;

import com.apihub.backend.features.users.domain.Role;
import com.apihub.backend.features.users.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final RolePermissionPolicy policy;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return false;
        }

        PermissionCode permissionCode;
        try {
            permissionCode = PermissionCode.valueOf(permission.toString().toUpperCase());
        } catch (IllegalArgumentException e) {
            return false;
        }

        for (Role role : user.getRoles()) {
            if (policy.hasPermission(role.getName(), permissionCode)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return hasPermission(authentication, null, permission);
    }
}
