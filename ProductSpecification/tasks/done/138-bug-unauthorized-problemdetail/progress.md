# Task 138: 401 UnauthorizedEntryPoint ProblemDetail -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: 401 unauthenticated-entry returns RFC-9457 ProblemDetail (issue #138)
Pure security-infrastructure fix (the `AuthenticationEntryPoint` in `SecurityConfig`). No
domain/usecase logic, so `red-usecase` / `green-usecase` are `[S]`. Twin of #130 / Task 13
(`ProblemDetailAccessDeniedHandler`). Full-stack journey: no-impact (backend error-shape only).
- [x] red-acceptance (UnauthorizedEntryPointIntegrationTest, tag #138 — RED @ExpectedToFail; legacy {code,message} vs 401 ProblemDetail)
- [x] design (see decisions/unauthorized-problemdetail-decision.md — mirrors Task 13 ProblemDetailAccessDeniedHandler)
- [S] red-usecase (no usecase/domain change — infrastructure-only)
- [S] green-usecase
- [x] adapters-discovery (security entry-point adapter in SecurityConfig)
  - Check 1 (ports): [S] — infrastructure-only fix, no usecase, no outbound ports
  - Check 2 (exceptions): [S] — AuthenticationException is framework-thrown, dispatched by ExceptionTranslationFilter to the AuthenticationEntryPoint, not a @ControllerAdvice mapping
  - Check 3 (response shape): [S] — simple delegation (fixed 401 ProblemDetail, no validation/error-mapping); acceptance test covers behavior + wiring; ProblemDetailAuthenticationEntryPoint + ErrorConstants.UNAUTHORIZED_TYPE + SecurityConfig rewiring created in green-acceptance
- [x] green-acceptance (ProblemDetailAuthenticationEntryPoint + ErrorConstants.UNAUTHORIZED_TYPE, rewired SecurityConfig; UnauthorizedEntryPointIntegrationTest GREEN — 1 pass)
