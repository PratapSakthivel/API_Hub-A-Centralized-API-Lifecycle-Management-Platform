# Feature: In-App Notification Center & Alert System

## Branch: `feature/notifications`

## Overview

The **Notification System** equips the API Hub with asynchronous alert broadcasting and event tracking. Developers and administrators receive real-time updates regarding API lifecycle milestones, breaking version deprecations, test run regressions, and scheduled infrastructure windows.

## Key Capabilities

| Capability | Description |
|---|---|
| Alert Severities | Categorized notifications across `INFO`, `SUCCESS`, `WARNING`, and `ALERT` tiers |
| Targeted & Broadcast Messaging | Direct user-specific alerts or platform-wide broadcast announcements |
| Event Categorization | Grouped into `LIFECYCLE`, `TESTING`, `RELEASE`, `SECURITY`, and `ANNOUNCEMENT` |
| Read Status Tracking | Live unread counters, single-item dismissals, and bulk "Mark All as Read" actions |
| Direct Navigation Actions | Interactive `actionUrl` links directing recipients straight to the relevant API or test run |

## Files Added

### Backend
| File | Purpose |
|---|---|
| `features/notifications/domain/NotificationType.java` | Severity enum (`INFO`, `SUCCESS`, `WARNING`, `ALERT`) |
| `features/notifications/domain/Notification.java` | JPA entity with indexes on recipient, read status, and timestamp |
| `features/notifications/repository/NotificationRepository.java` | Targeted queries, unread counters, and bulk update operations |
| `features/notifications/dto/CreateNotificationRequest.java` | Request DTO for dispatching notifications |
| `features/notifications/dto/NotificationDto.java` | Full response DTO |
| `features/notifications/service/NotificationService.java` | Core alert management service |
| `features/notifications/controller/NotificationController.java` | REST endpoints for alerts, unread counts, and status updates |

### Frontend
| File | Purpose |
|---|---|
| `src/features/notifications/NotificationCenterView.tsx` | Full-featured notification center with unread filtering, badges, dismissals, and broadcast modal |

### Tests
| File | Coverage |
|---|---|
| `notifications/NotificationServiceTest.java` | Targeted creation, unread counting, status modification, and validation |

## API Endpoints

```
GET   /api/v1/notifications               → List notifications (optional ?userId=, ?unreadOnly=)
GET   /api/v1/notifications/unread-count  → Return pending unread counter
POST  /api/v1/notifications               → Create or broadcast a notification
PATCH /api/v1/notifications/{id}/read     → Mark a specific notification as read
PATCH /api/v1/notifications/read-all      → Mark all unread notifications as read
```
