# Feature: Environment & Configuration Management

## Branch: `feature/environment-management`

## Overview

This module delivers **Environment and Configuration Governance**, allowing engineering teams to manage target deployment hostnames, base URLs, and environment variables across `DEV`, `STAGING`, `PRODUCTION`, and `SANDBOX` tiers, with built-in secret masking protection.

## Key Capabilities

| Capability | Description |
|---|---|
| Multi-Tier Environments | Configure hostnames and target base URLs per environment code (`DEV`, `STAGING`, `PRODUCTION`, `SANDBOX`) |
| Global or API-Scoped | Environments can be scoped to a single API or registered globally across the entire platform |
| Secret Masking | Sensitive credentials (tokens, private keys) are tagged as `secret` and masked with `••••••••` by default |
| On-Demand Secret Auditing | Authorized calls can unmask values via explicit query parameter |
| Dynamic Activation Toggle | Quickly disable environments under maintenance without deleting associated configurations |

## Files Added

### Backend
| File | Purpose |
|---|---|
| `features/environments/domain/ApiEnvironment.java` | JPA entity representing an execution environment |
| `features/environments/domain/EnvironmentVariable.java` | Key-value configuration entry with secret flag |
| `features/environments/repository/ApiEnvironmentRepository.java` | Queries by API ID, code, and global scopes |
| `features/environments/repository/EnvironmentVariableRepository.java` | Queries by environment and key |
| `features/environments/dto/CreateEnvironmentRequest.java` | Request DTO for creating an environment |
| `features/environments/dto/EnvironmentDto.java` | Full environment response DTO |
| `features/environments/dto/EnvironmentVariableDto.java` | Variable DTO with secret flag |
| `features/environments/service/EnvironmentService.java` | CRUD business logic and secret masking enforcement |
| `features/environments/controller/EnvironmentController.java` | REST endpoints for environments and variables |

### Frontend
| File | Purpose |
|---|---|
| `src/features/environments/EnvironmentManagementView.tsx` | Interactive UI with environment cards, status toggles, inline variable editor, and secret eye toggle |

### Tests
| File | Coverage |
|---|---|
| `environments/EnvironmentServiceTest.java` | Creation, secret masking enforcement, unmask toggle, status modification |

## API Endpoints

```
GET    /api/v1/environments                   → List environments (optional ?apiId=, ?unmaskSecrets=)
GET    /api/v1/environments/{id}              → Get single environment
POST   /api/v1/environments                   → Create new environment
PATCH  /api/v1/environments/{id}/toggle-status → Enable or disable environment
POST   /api/v1/environments/{id}/variables     → Add or update an environment variable
DELETE /api/v1/environments/{id}               → Delete an environment
```
