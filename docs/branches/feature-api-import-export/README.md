# Feature: OpenAPI 3.x Import & Export Engine

## Branch: `feature/api-import-export`

## Overview

This module provides **specification interoperability** for the API Hub, enabling teams to import arbitrary OpenAPI 3.x JSON definitions directly into the platform to create APIs, versions, and documented endpoints, as well as export existing versioned contracts into downloadable OpenAPI 3.0 artifacts.

## Key Capabilities

| Capability | Description |
|---|---|
| Ingest OpenAPI 3.x Specs | Parses JSON payloads extracting metadata (`title`, `description`, `version`) and all defined paths |
| Automatic Route Synthesis | Creates structured `ApiEndpoint` entities with HTTP methods, paths, operation IDs, summaries, and responses |
| Version-Scoped Export | Generates conformant OpenAPI 3.0.3 specification JSON for any version |
| Downloadable Artifacts | HTTP `Content-Disposition: attachment` header support for one-click `.json` downloads |
| Conflict-Resistant Slugs | Automatically de-duplicates and normalizes URL slugs during batch imports |

## Files Added

### Backend
| File | Purpose |
|---|---|
| `features/impexp/dto/ImportApiRequest.java` | Inbound DTO carrying raw OpenAPI specification text and optional project association |
| `features/impexp/dto/ImportResultDto.java` | Summary DTO returning the created API ID, version, and endpoints count |
| `features/impexp/service/OpenApiImportExportService.java` | Core parsing and serialization engine |
| `features/impexp/controller/ApiImportExportController.java` | `POST /api/v1/apis/import` and `GET /api/v1/versions/{id}/export` endpoints |

### Frontend
| File | Purpose |
|---|---|
| `src/features/impexp/ApiImportExportView.tsx` | Dual-tab interface supporting specification ingestion with quick sample presets, alongside JSON preview and download |

### Tests
| File | Coverage |
|---|---|
| `impexp/OpenApiImportExportServiceTest.java` | Verifies parser logic, endpoint generation, slug uniqueness, and serialization |

## API Endpoints

```
POST /api/v1/apis/import                     → Import OpenAPI 3.x JSON specification
GET  /api/v1/versions/{versionId}/export     → Download OpenAPI specification file
GET  /api/v1/versions/{versionId}/export/preview → Return spec JSON in API response
```
