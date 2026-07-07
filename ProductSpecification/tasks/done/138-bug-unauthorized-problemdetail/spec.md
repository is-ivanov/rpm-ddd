# Task 138: 401 UnauthorizedEntryPoint returns legacy body, not RFC-9457 ProblemDetail

Type: bug
Issue: #138

## Problem

The same class of bug as #130 (Task 13, done), but on the **401 / unauthenticated-entry** path.
When an unauthenticated request hits a protected endpoint, Spring Security's
`ExceptionTranslationFilter` dispatches to the configured `AuthenticationEntryPoint`.
`SecurityConfig` wires the wimdeblauwe `UnauthorizedEntryPoint`, which serializes the library's
**native** `ApiErrorResponse` shape:

```json
{"code":"...","message":"..."}
```

instead of RFC-9457 ProblemDetail. Like `ApiErrorResponseAccessDeniedHandler` (fixed in #130 /
PR #137), this servlet/filter-level handler ignores `use-problem-detail-format: true` — that flag
only affects the `@ControllerAdvice` path.

Note: the **401 on `POST /api/auth/login` with bad credentials** already returns correct RFC-9457 —
that goes through the `@ControllerAdvice` path (`UserAuthenticationException` → ProblemDetail),
**not** the entry point. This bug is specifically the entry point that fires for *unauthenticated
access to a protected endpoint*.

This violates `.claude/rules/coding-rules.md` → Error Handling: **all** error responses, including
those from the security filter chain, must be RFC-9457 ProblemDetail (rule strengthened in PR #137).

## Solution

Replace `UnauthorizedEntryPoint` with a custom `AuthenticationEntryPoint` that emits an RFC-9457
`ProblemDetail` as `application/problem+json` (401, title "Unauthorized",
`https://www.rpm-ddd.my/problem/unauthorized` type, `instance` = request URI), mirroring
`ProblemDetailAccessDeniedHandler` introduced in #130. Add a `UNAUTHORIZED_TYPE` constant to
`ErrorConstants` and rewire `SecurityConfig` to use the new entry point.

Pure security-infrastructure fix — no domain/usecase logic, so `red-usecase` / `green-usecase` are
`[S]`, exactly as in Task 13.

## Key Files

- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/security/SecurityConfig.java` — rewire the entry point bean
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/security/ProblemDetailAuthenticationEntryPoint.java` — new (mirrors `ProblemDetailAccessDeniedHandler`)
- `src/main/java/by/iivanov/rpm/shared/infrastructure/web/errors/ErrorConstants.java` — add `UNAUTHORIZED_TYPE`
- `src/test/java/by/iivanov/rpm/iam/user/` — new `red-acceptance` test tagged `#138`

## Full-stack journey verdict

**no-impact** — backend-only error-shape fix on the 401 entry point. It does not change a rendered
critical user-lifecycle path; the top-tier full-stack journey does not exercise unauthenticated
access to a protected `/api/**` endpoint.

## Reproduction

1. Start the backend.
2. Send an unauthenticated request to a protected endpoint, e.g. `GET /api/...` (no session cookie).
3. Observe the 401 response body is the legacy `{"code":"...","message":"..."}` shape with
   `Content-Type: application/json`, instead of an RFC-9457 `ProblemDetail`
   (`application/problem+json` with `type`/`title`/`status`/`detail`/`instance`).
