package com.apihub.backend.security.policy;

public enum PermissionCode {
    // User / Admin Permissions
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Project & Catalog Permissions
    PROJECT_CREATE,
    PROJECT_UPDATE,
    API_READ,
    API_CREATE,
    API_UPDATE,
    API_DELETE,

    // Versioning & Documentation Permissions
    VERSION_CREATE,
    VERSION_PUBLISH,
    VERSION_LIFECYCLE,
    ENDPOINT_CREATE,
    ENDPOINT_UPDATE,

    // Testing Permissions
    TEST_READ,
    TEST_CREATE,
    TEST_EXECUTE,

    // Release & Governance
    RELEASE_VIEW,
    RELEASE_CREATE,
    RELEASE_APPROVE,

    // Dependency Mapping
    DEPENDENCY_VIEW,
    DEPENDENCY_MANAGE
}
