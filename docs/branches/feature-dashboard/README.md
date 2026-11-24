# Feature: Portfolio Dashboard & Real-Time Analytics

## Branch: `feature/dashboard`

## Overview

The **Dashboard Module** aggregates real-time operational data across the entire API platform to provide unified portfolio visibility for engineering leads, architects, and administrators.

## Core Capabilities

| Capability | Description |
|---|---|
| Portfolio Metrics | High-level counts of APIs, active projects, contributors, and test suites |
| Lifecycle Distribution | Live breakdown of APIs by lifecycle status (`PLANNING`, `DRAFT`, `ACTIVE`, `DEPRECATED`, `RETIRED`) |
| Release Governance Pipeline | Track distribution of APIs currently in `DEVELOPMENT`, `TESTING`, `STAGING`, and `PRODUCTION` |
| Automated Test Health | Aggregated test run statistics, pass/fail counts, and calculated pass percentage |
| Real-Time Audit Feed | Stream of recent administrative operations, status changes, and test executions |

## Files Added

### Backend
| File | Purpose |
|---|---|
| `features/dashboard/dto/RecentActivityDto.java` | DTO representing individual audited actions |
| `features/dashboard/dto/DashboardSummaryDto.java` | Aggregated response DTO containing all platform metrics |
| `features/dashboard/service/DashboardService.java` | Core analytics service aggregating from all repositories |
| `features/dashboard/controller/DashboardController.java` | `GET /api/v1/dashboard/summary` endpoint |

### Frontend
| File | Purpose |
|---|---|
| `src/features/dashboard/DashboardView.tsx` | Responsive dashboard with KPI cards, progress meters, stage breakdown, and audit table |

### Tests
| File | Purpose |
|---|---|
| `features/dashboard/DashboardServiceTest.java` | Unit tests for aggregation logic, percentage calculation, and empty dataset handling |

## API Endpoints

```
GET /api/v1/dashboard/summary  → Returns DashboardSummaryDto
```
