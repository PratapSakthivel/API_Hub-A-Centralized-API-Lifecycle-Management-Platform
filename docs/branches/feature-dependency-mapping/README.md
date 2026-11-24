# Feature: Dependency Mapping

## Branch: `feature/dependency-mapping`

## Overview

This module implements **API Dependency Mapping** — a graph-based system to track, visualize, and analyze directional dependencies between APIs in the hub.

## Key Capabilities

| Capability | Description |
|---|---|
| Dependency Graph | Store directed `source → target` relationships between APIs |
| Relationship Types | `DEPENDS_ON`, `EXTENDS`, `USES`, `INCLUDES` |
| Cycle Detection | BFS-based detection prevents circular dependency chains |
| Impact Analysis | Determines all APIs downstream of a given API (transitive) |
| Self-Reference Guard | Prevents an API from depending on itself |
| Duplicate Guard | Unique constraint on `(source_api_id, target_api_id)` |

## Files Added

### Backend

| File | Purpose |
|---|---|
| `features/dependencies/ApiDependency.java` | JPA entity — `source_api_id`, `target_api_id`, `relationship_type` |
| `features/dependencies/ApiDependencyRepository.java` | Queries: findBySourceApiId, findByTargetApiId, existsBySourceAndTarget |
| `features/dependencies/dto/CreateDependencyRequest.java` | Request DTO for creating a dependency |
| `features/dependencies/dto/DependencyDto.java` | Response DTO with API names resolved |
| `features/dependencies/dto/ImpactAnalysisDto.java` | Downstream impact summary with transitive chain |
| `features/dependencies/service/DependencyService.java` | Core logic: create, remove, cycle detection, BFS impact analysis |
| `features/dependencies/controller/DependencyController.java` | REST endpoints |

### Frontend

| File | Purpose |
|---|---|
| `features/dependencies/DependencyMapView.tsx` | UI to explore upstream/downstream deps and impact analysis |

### Tests

| File | Coverage |
|---|---|
| `DependencyServiceTest.java` | createDependency success, self-reference, duplicate, cycle, remove |

## API Endpoints

```
GET  /api/v1/apis/{apiId}/dependencies/upstream   → List upstream deps
GET  /api/v1/apis/{apiId}/dependencies/downstream  → List downstream deps
GET  /api/v1/apis/{apiId}/dependencies/impact      → Impact analysis
POST /api/v1/dependencies                           → Create dependency
DELETE /api/v1/dependencies/{id}                   → Remove dependency
```

## Commits

1. `feat: manage directional API dependency relationships` — Entity + Repository
2. `feat: implement cycle detection and downstream impact analysis` — DTOs + Service + Controller
3. `feat: visualize API dependency graph and impact analysis` — Frontend component
4. `test: cover cycle detection and impact analysis logic` — Tests + Docs
