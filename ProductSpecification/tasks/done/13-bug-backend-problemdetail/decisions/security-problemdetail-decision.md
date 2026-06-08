# Decision: Security filter-chain errors emit RFC-9457 ProblemDetail via custom handlers

**Date**: 2026-06-08 **Scenarios**: Task 13 / issue #130

Why: the wimdeblauwe `ApiErrorResponseAccessDeniedHandler` / `UnauthorizedEntryPoint` serialize the legacy `{code,message}` shape and ignore `use-problem-detail-format: true` (that flag only affects the `@ControllerAdvice` path), so the security filter-chain violates the "all errors are RFC-9457 ProblemDetail" rule.

| Rejected | Why |
|----------|-----|
| Delegate to `HandlerExceptionResolver` (route security exceptions through the existing `@ControllerAdvice`) | Pinning the exact custom CSRF `detail`/`type` through the generic advice is awkward; invoking the MVC resolver from inside the filter chain adds runtime subtleties for a small fixed body. |
| `error.handling.handle-filter-chain-exceptions=true` | Covers exceptions *thrown by* filters, not the `ExceptionTranslationFilter → AccessDeniedHandler` dispatch; RED proved the legacy handler is still invoked. |
| Keep library handlers (status quo) | Emits legacy `{code,message}` — the bug itself. |

**Chosen**: Custom `iam.auth.infrastructure` handlers implementing `AccessDeniedHandler` (and, for consistency, `AuthenticationEntryPoint`) that build a Spring `ProblemDetail` and write it as `application/problem+json` via the injected Jackson `ObjectMapper`. Wire them in `SecurityConfig` in place of the wimdeblauwe beans.

## Model

- New: `ProblemDetailAccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler` — builds `ProblemDetail.forStatus(403)` (title auto = "Forbidden"), sets `type` = `ErrorConstants.ACCESS_DENIED_TYPE`, `detail` = "Access denied: a valid CSRF token is required for this request.", `instance` = request URI; writes `application/problem+json` + 403.
- New constant: `ErrorConstants.ACCESS_DENIED_TYPE` = `PROBLEM_BASE_URL + "/access-denied"`.
- `SecurityConfig`: replace the `accessDeniedHandler` bean (drop the `ApiErrorResponseAccessDeniedHandler` wiring); keep the `exceptionHandling` wiring.
- The custom handler must NOT leak the raw CSRF exception message — the `detail` is a fixed human-readable string.

## Edge Cases

| Case | Behavior |
|------|----------|
| 401 `UnauthorizedEntryPoint` (unauthenticated access to protected endpoint) | Same legacy-shape bug, but NOT covered by the current RED test. Out of scope for this step — needs its own red-acceptance scenario before its handler is replaced (else untested production code). Decide separately. |
| Non-CSRF 403 (authenticated-but-unauthorized) | Same generic `access-denied` type; the CSRF-specific `detail` wording is acceptable here because the SPA's realistic 403 is CSRF. If non-CSRF 403s become common, branch `detail` by exception type later. |
