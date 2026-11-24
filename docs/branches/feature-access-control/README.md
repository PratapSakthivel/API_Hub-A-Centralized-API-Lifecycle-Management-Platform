# Branch Contribution: feature/access-control

## Branch & Module Details
- **Branch**: `feature/access-control`
- **Module**: Access Control & Granular RBAC Governance

## Purpose
Establishes a centralized permission matrix across the platform's 5 user roles (`ADMIN`, `DEVELOPER`, `QA_ENGINEER`, `PROJECT_MANAGER`, `CONSUMER`), providing backend authorization checks through Spring Security and frontend UX guards.

## Features Added
- **Centralized Permission Registry**: `PermissionCode` enum defining operations across users, catalog, documentation, versions, testing, dependencies, and release stages.
- **Role Permission Policy**: `RolePermissionPolicy` mapping all 5 roles strictly to their sanctioned capabilities.
- **Permission Evaluator**: Spring Security `CustomPermissionEvaluator` enabling SpEL expressions like `@PreAuthorize("hasPermission(null, 'API_CREATE')")`.
- **Frontend Guard**: `RoleGuard.tsx` component preventing unauthorized actions and rendering accessible views according to user roles.

## Verification
- Unit test coverage in `backend/src/test/java/com/apihub/backend/security/policy/AccessControlPolicyTest.java`.
