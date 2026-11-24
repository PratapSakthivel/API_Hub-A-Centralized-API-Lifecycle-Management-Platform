# Branch Contribution: feature/api-documentation

## Branch & Module Details
- **Branch**: `feature/api-documentation`
- **Module**: Endpoint Documentation & OpenAPI Generation

## Purpose
Enables engineering teams to document API endpoints, their HTTP methods, paths, parameters, schemas, and status codes scoped strictly to specific API versions, with dynamic OpenAPI 3.x document export.

## Features Added
- **Domain Persistence**: `ApiVersion` entity (with immutability rules) and `ApiEndpoint` entity with composite unique constraint `(api_version_id, path, http_method)`.
- **Validation**: Enforces standard HTTP method verbs, leading slashes on paths, and rejects duplicate paths on the same version.
- **OpenAPI 3.x Engine**: `OpenApiGeneratorService` dynamically aggregates version-scoped endpoints into standard OpenAPI specification objects.

## Endpoints Added
- `GET /api/v1/versions/{versionId}/endpoints`
- `GET /api/v1/endpoints/{id}`
- `POST /api/v1/versions/{versionId}/endpoints`
- `GET /api/v1/versions/{versionId}/openapi`

## Automated Tests
- Unit test coverage in `backend/src/test/java/com/apihub/backend/features/documentation/OpenApiGeneratorServiceTest.java`.
