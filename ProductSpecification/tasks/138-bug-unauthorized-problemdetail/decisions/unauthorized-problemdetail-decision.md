# Decision: 401 unauthenticated-entry emits RFC-9457 ProblemDetail via a custom AuthenticationEntryPoint

**Date**: 2026-07-07 **Task**: 138 / issue #138

Why: Spring Security's `ExceptionTranslationFilter` dispatches unauthenticated access to a protected
endpoint to the configured `AuthenticationEntryPoint`. `SecurityConfig` wires the wimdeblauwe
`UnauthorizedEntryPoint`, which serializes the legacy `{code,message}` shape and ignores
`use-problem-detail-format: true` (that flag only affects the `@ControllerAdvice` path). This violates
the "all errors are RFC-9457 ProblemDetail" rule (`.claude/rules/coding-rules.md` → Error Handling).

This is the **401 twin of #130 / Task 13**, which fixed the 403 (`AccessDeniedHandler`) path with
`ProblemDetailAccessDeniedHandler`. The Task 13 ADR
(`ProductSpecification/tasks/done/13-bug-backend-problemdetail/decisions/security-problemdetail-decision.md`)
already evaluated and rejected the alternatives — they apply identically here:

| Rejected | Why |
|----------|-----|
| Route through `HandlerExceptionResolver` / existing `@ControllerAdvice` | Pinning the exact custom `detail`/`type` through the generic advice is awkward; invoking the MVC resolver from inside the filter chain adds runtime subtleties for a small fixed body. |
| `error.handling.handle-filter-chain-exceptions=true` | Covers exceptions *thrown by* filters, not the `ExceptionTranslationFilter → AuthenticationEntryPoint` dispatch. |
| Keep the wimdeblauwe `UnauthorizedEntryPoint` (status quo) | Emits legacy `{code,message}` — the bug itself. |

**Chosen**: a custom `iam.user.infrastructure.security.ProblemDetailAuthenticationEntryPoint`
implementing `org.springframework.security.web.AuthenticationEntryPoint` that builds a Spring
`ProblemDetail` and writes it as `application/problem+json` via the injected Jackson `ObjectMapper`,
mirroring `ProblemDetailAccessDeniedHandler`. Wire it in `SecurityConfig` in place of the wimdeblauwe
bean.

## Model

- New: `ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint` —
  `commence(...)` builds `ProblemDetail.forStatus(401)` (title auto = "Unauthorized"), sets
  `type` = `ErrorConstants.UNAUTHORIZED_TYPE`, `detail` = "Authentication is required to access this
  resource.", `instance` = request URI; writes `application/problem+json` + 401.
- New constant: `ErrorConstants.UNAUTHORIZED_TYPE` = `PROBLEM_BASE_URL + "/unauthorized"`.
- `SecurityConfig`: replace the `UnauthorizedEntryPoint` bean (drop the wimdeblauwe
  `HttpStatusMapper` / `ErrorCodeMapper` / `ErrorMessageMapper` wiring + imports); keep the
  `exceptionHandling().authenticationEntryPoint(...)` wiring.
- The custom entry point must NOT leak the raw `AuthenticationException` message — `detail` is a
  fixed human-readable string.

## Edge Cases

| Case | Behavior |
|------|----------|
| 401 on `POST /api/auth/login` with bad credentials | Unchanged — already correct RFC-9457 via the `@ControllerAdvice` path (`UserAuthenticationException`), NOT the entry point. This task does not touch it. |
| Different `AuthenticationException` subtypes (expired session, missing session) | All produce the same generic 401 `unauthorized` ProblemDetail; the fixed `detail` wording is intentional and does not branch by exception type. |
