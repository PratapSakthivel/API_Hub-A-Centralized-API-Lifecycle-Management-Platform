# Feature: Release Management

## Branch: `feature/release-management`

## Overview

This module implements a **4-stage API Release Pipeline** — a promotion workflow that takes APIs from development through testing and staging to production, with approval tracking at each stage.

## Pipeline Stages

```
DEVELOPMENT → TESTING → STAGING → PRODUCTION
```

| Stage | Description |
|---|---|
| `DEVELOPMENT` | Default starting stage for all new releases |
| `TESTING` | Quality assurance and integration testing |
| `STAGING` | Pre-production environment validation |
| `PRODUCTION` | Live, customer-facing deployment |

## Key Rules
- Releases **must** be promoted in sequential order (no stage skipping)
- Once in `PRODUCTION`, no further promotion is allowed
- Each promotion records the approver name and timestamp

## Files Added

### Backend

| File | Purpose |
|---|---|
| `release/domain/ApiRelease.java` | JPA entity — `api`, `version`, `stage`, `approvedBy`, `promotedAt` |
| `release/domain/ReleaseStage.java` | Enum with ordered stage validation via `next()` method |
| `release/repository/ApiReleaseRepository.java` | Queries by apiId, latest release |
| `release/dto/CreateReleaseRequest.java` | Request DTO for new releases |
| `release/dto/PromoteReleaseRequest.java` | Promote request with targetStage and approvedBy |
| `release/dto/ReleaseDto.java` | Full response DTO |
| `release/service/ReleaseService.java` | Business logic: create, promote with stage validation |
| `release/controller/ReleaseController.java` | REST endpoints |

### Frontend

| File | Purpose |
|---|---|
| `features/release/ReleaseManagement.tsx` | Kanban-style 4-column stage board with promote buttons |

### Tests

| File | Coverage |
|---|---|
| `ReleaseServiceTest.java` | Create, dev→testing, skip-stage error, production guard, 404 |

## API Endpoints

```
GET  /api/v1/apis/{apiId}/releases       → List releases for an API
POST /api/v1/releases                    → Create a new release
POST /api/v1/releases/{id}/promote       → Promote release to next stage
```
