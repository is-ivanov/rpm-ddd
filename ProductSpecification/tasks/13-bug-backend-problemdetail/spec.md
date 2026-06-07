# Task 13: Backend Security Errors in ProblemDetail

Type: bug
Issue: #130

## Problem

Not all backend error responses use RFC-9457 ProblemDetail. The CSRF / access-denied path returns
the wimdeblauwe error-handling-starter's **native** shape:

```json
{"code":"INVALID_CSRF_TOKEN","message":"Invalid CSRF Token 'null' ..."}
```

while the authentication path correctly returns RFC-9457:

```json
{"type":"https://www.rpm-ddd.my/problem/authentication-failed","title":"Unauthorized","status":401,"detail":"Account not activated","instance":"/api/auth/login"}
```

Confirmed live: `POST /api/auth/login` without a CSRF token → 403 with the `{code,message}` body.

## Root cause

`application.yml` sets `use-problem-detail-format: true`, honored by the `@ExceptionHandler` path.
But the **servlet/filter-level** handlers wired in `SecurityConfig` — `ApiErrorResponseAccessDeniedHandler`
(and to re-verify, `UnauthorizedEntryPoint`) — serialize the legacy `ApiErrorResponse` and ignore
the problem-detail setting. Violates `.claude/rules/coding-rules.md` → Error Handling.

## Solution

Make the security filter-chain error handlers (access-denied / CSRF, authentication entry point)
emit RFC-9457 ProblemDetail consistently with the rest of the app. Also **strengthen
`coding-rules.md`** (Error Handling) to state explicitly that ALL error responses — including those
produced by the security filter chain and the framework — must be RFC-9457 ProblemDetail; the
library/framework native shape is forbidden.

Independent of Task 12: even once login sends a CSRF token, any access-denied response must be
ProblemDetail.

## Key Files

- `src/main/java/by/iivanov/rpm/iam/auth/infrastructure/SecurityConfig.java`
- error-handling-starter config (a custom `AccessDeniedHandler` / `AuthenticationEntryPoint` that
  emits ProblemDetail, if the starter cannot be configured to do so)
- `src/main/resources/application.yml`
- `.claude/rules/coding-rules.md` (strengthen the Error Handling rule)

## Reproduction

1. `curl -i -X POST https://rpm-ddd.onrender.com/api/auth/login -H 'Content-Type: application/json' -d '{"login":"x","password":"y"}'`
2. Response: `403` with body `{"code":"INVALID_CSRF_TOKEN","message":"..."}` — not ProblemDetail.
