# Branch Contribution: feature/api-version-management

## Branch & Module Details
- **Branch**: `feature/api-version-management`
- **Module**: API Version Management & Immutability Engine

## Purpose
Ensures APIs can be evolved without breaking consumers by supporting semantic versioning (`MAJOR.MINOR.PATCH`), immutable published snapshots, version diff comparison, and lifecycle management (`DRAFT`, `PUBLISHED`, `DEPRECATED`, `RETIRED`).

## Features Added
- **Semantic Versioning Record**: `SemanticVersion` validation utility ensuring strict semantic label compliance.
- **Draft Creation & Immutability**: New changes occur in new draft versions. When marked `PUBLISHED`, the version snapshot is frozen.
- **Version Comparison Engine**: Endpoint comparison engine comparing base vs target version to compute added, removed, and modified endpoints.
- **Deprecation & Retirement**: Supports marking published versions as `DEPRECATED` or `RETIRED`.
- **Frontend UI**: `VersionManagement.tsx` component with version table, action buttons, diff calculation widget, and creation modal.

## Endpoints Added
- `GET /api/v1/apis/{apiId}/versions`
- `GET /api/v1/versions/{versionId}`
- `POST /api/v1/apis/{apiId}/versions`
- `POST /api/v1/versions/{versionId}/publish`
- `PATCH /api/v1/versions/{versionId}/lifecycle?status={status}`
- `GET /api/v1/versions/compare?fromId={fromId}&toId={toId}`

## Automated Tests
- Unit test coverage in `backend/src/test/java/com/apihub/backend/features/versioning/VersionServiceTest.java`.
