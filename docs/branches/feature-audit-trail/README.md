# Branch Contribution: feature/audit-trail

## Branch & Module Details
- **Branch**: `feature/audit-trail`
- **Module**: Change Log & Audit Trail

## Purpose
Provides immutable, append-only audit trail logging for security, compliance, and lifecycle governance. Captures actor identity, action type, target entity, timestamp, client IP, and sanitized payload details.

## Features Added
- **Append-Only Domain**: `AuditLog` entity indexed by actor, entity type/id, and timestamp.
- **Sensitive Data Masking**: `AuditService` regex scrubbing preventing raw credentials, JWTs, API keys, and passwords from ever entering log records.
- **Transactional Independence**: `Propagation.REQUIRES_NEW` guarantees audit trail creation even across isolated domain events.
- **Query & Filter Endpoints**: Filterable audit trail queries restricted to authorized administrators and project managers (`GET /api/v1/audit-logs`).

## Endpoints Added
- `GET /api/v1/audit-logs` (supports filters: `entityType`, `actorEmail`, pagination)

## Verification
- Unit test coverage in `backend/src/test/java/com/apihub/backend/features/audit/AuditServiceTest.java`.
