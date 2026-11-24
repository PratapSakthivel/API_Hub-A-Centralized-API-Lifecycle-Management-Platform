# Branch Contribution: feature/api-catalog

## Branch & Module Details
- **Branch**: `feature/api-catalog`
- **Module**: Project & API Catalog Management

## Purpose
Provides centralized capabilities to register, organize, search, filter, and view APIs across organizational projects. Enforces unique slug validation and ties APIs to projects and owners.

## Features Implemented
- **Project Domain**: `Project` entity with `projectKey`, name, description, and status.
- **API Domain**: `ApiEntity` with `slug`, `name`, `summary`, `lifecycleStatus` (`DRAFT`, `ACTIVE`, `DEPRECATED`, `RETIRED`), and relations to `Project` and `User`.
- **Search & Pagination**: Server-side filtering by project ID, lifecycle status, and keyword query over name, slug, and summary.
- **Frontend UI**: Integrated `ApiCatalog.tsx` component with search bar, status dropdown, responsive cards, and dynamic registration modal.

## Endpoints Added
- `GET /api/v1/projects`
- `GET /api/v1/projects/{id}`
- `POST /api/v1/projects`
- `GET /api/v1/apis` (supports query params: `projectId`, `status`, `q`, `page`, `size`)
- `GET /api/v1/apis/{id}`
- `POST /api/v1/apis`

## Automated Tests
- Unit testing in `backend/src/test/java/com/apihub/backend/features/catalog/ApiCatalogServiceTest.java`.
