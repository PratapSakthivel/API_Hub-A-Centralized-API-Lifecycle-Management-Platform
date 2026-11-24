# Branch Contribution: feature/user-management

## Branch & Module Details
- **Branch**: `feature/user-management`
- **Module**: User Management & Authentication

## Purpose
Implements user account persistence, roles, password encryption with BCrypt, JWT token issuance, and administrator operations for viewing, creating, and updating accounts.

## Features Added
- Defined domain entities: `User` and `Role` with the 5 platform roles (`ADMIN`, `DEVELOPER`, `QA_ENGINEER`, `PROJECT_MANAGER`, `CONSUMER`).
- Stateless authentication with `JwtProvider` and `JwtAuthenticationFilter`.
- `AuthService` and `AuthController` with `/api/v1/auth/login`, `/api/v1/auth/me`, and `/api/v1/auth/logout`.
- `UserService` and `UserController` with role-restricted endpoints for listing, creating, and updating users.
- Automated data initialization for default roles and the initial bootstrap admin user.

## Endpoints Added
- `POST /api/v1/auth/login`
- `GET /api/v1/auth/me`
- `POST /api/v1/auth/logout`
- `GET /api/v1/users`
- `POST /api/v1/users`
- `GET /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}`
- `PATCH /api/v1/users/{id}/status`

## Verification
- Unit test coverage implemented in `backend/src/test/java/com/apihub/backend/features/users/UserServiceTest.java`.
