# Branch Contribution: feature/api-testing

## Branch & Module Details
- **Branch**: `feature/api-testing`
- **Module**: Automated API Testing & SSRF Protection Engine

## Purpose
Provides end-to-end automated testing for APIs and versions with strict security mitigations against Server-Side Request Forgery (SSRF). Tracks execution metrics, pass/fail counts, duration, and response outcomes.

## Features Added
- **SSRF Defense Engine**: `SafeHttpDestinationValidator` enforcing HTTP/HTTPS schemes only, blocking link-local and internal private IP spaces.
- **Outbound HTTP Runner**: `SafeHttpExecutor` with connection timeouts, redirect prevention (`Redirect.NEVER`), response size limits (truncation at 2KB), and sensitive header/cookie redaction.
- **Test Management & Execution**: `TestSuite`, `TestCase`, `TestRun`, and `TestResult` entities capturing overall status (`PASSED`, `FAILED`), execution duration, and individual assertion outcomes.
- **Frontend UI**: `ApiTestingView.tsx` with case configuration, execution trigger, and run history tracker.

## Endpoints Added
- `GET /api/v1/apis/{apiId}/test-suites`
- `POST /api/v1/test-suites`
- `GET /api/v1/test-suites/{suiteId}/cases`
- `POST /api/v1/test-cases`
- `POST /api/v1/test-suites/{suiteId}/run`
- `GET /api/v1/apis/{apiId}/test-runs`

## Automated Tests
- Unit test coverage in `backend/src/test/java/com/apihub/backend/features/testing/SafeHttpDestinationValidatorTest.java`.
