package com.apihub.backend.security.policy;

import com.apihub.backend.features.users.domain.Role;
import com.apihub.backend.features.users.domain.RoleName;
import com.apihub.backend.features.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AccessControlPolicyTest {

    private RolePermissionPolicy policy;
    private CustomPermissionEvaluator evaluator;

    @BeforeEach
    void setUp() {
        policy = new RolePermissionPolicy();
        evaluator = new CustomPermissionEvaluator(policy);
    }

    @Test
    void adminHasAllPermissions() {
        assertTrue(policy.hasPermission(RoleName.ADMIN, PermissionCode.USER_CREATE));
        assertTrue(policy.hasPermission(RoleName.ADMIN, PermissionCode.API_CREATE));
        assertTrue(policy.hasPermission(RoleName.ADMIN, PermissionCode.RELEASE_APPROVE));
    }

    @Test
    void consumerHasReadOnlyAccess() {
        assertTrue(policy.hasPermission(RoleName.CONSUMER, PermissionCode.API_READ));
        assertFalse(policy.hasPermission(RoleName.CONSUMER, PermissionCode.API_CREATE));
        assertFalse(policy.hasPermission(RoleName.CONSUMER, PermissionCode.TEST_EXECUTE));
        assertFalse(policy.hasPermission(RoleName.CONSUMER, PermissionCode.RELEASE_APPROVE));
    }

    @Test
    void qaCanTestButCannotModifyApiDefinitions() {
        assertTrue(policy.hasPermission(RoleName.QA_ENGINEER, PermissionCode.TEST_EXECUTE));
        assertFalse(policy.hasPermission(RoleName.QA_ENGINEER, PermissionCode.API_CREATE));
        assertFalse(policy.hasPermission(RoleName.QA_ENGINEER, PermissionCode.RELEASE_APPROVE));
    }

    @Test
    void permissionEvaluatorWithUserPrincipal() {
        Role devRole = Role.builder().name(RoleName.DEVELOPER).build();
        User devUser = User.builder().roles(Set.of(devRole)).build();
        Authentication auth = new UsernamePasswordAuthenticationToken(devUser, null);

        assertTrue(evaluator.hasPermission(auth, null, "API_CREATE"));
        assertFalse(evaluator.hasPermission(auth, null, "USER_CREATE")); // Only ADMIN can create users
    }
}
