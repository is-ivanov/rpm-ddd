# Task 13: Backend Security Errors in ProblemDetail -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: Security filter-chain errors return RFC-9457 ProblemDetail (issue #130)
Pure security-infrastructure fix (the error handlers in `SecurityConfig`). No domain/usecase logic,
so `red-usecase` / `green-usecase` are `[S]`. Step list may be refined by `/design-preview` at start
(e.g. web-slice vs acceptance level for asserting the 403 body shape).
- [x] red-acceptance (security: POST /api/auth/login without CSRF -> 403 body is RFC-9457 ProblemDetail; tag #130)
- [x] design (custom ProblemDetail handlers — see decisions/security-problemdetail-decision.md)
- [S] red-usecase (no usecase/domain change — infrastructure-only)
- [S] green-usecase
- [x] adapters-discovery (security error-handler adapters in SecurityConfig)
  - Check 1 (ports): [S] — infrastructure-only fix, no usecase, no outbound ports
  - Check 2 (exceptions): [S] — AccessDeniedException/CSRF is framework-thrown, handled by the filter-chain AccessDeniedHandler, not a @ControllerAdvice mapping
  - Check 3 (response shape): [S] — simple delegation (fixed 403 ProblemDetail, no validation/error-mapping); acceptance test covers behavior + wiring; custom AccessDeniedHandler + ErrorConstants.ACCESS_DENIED_TYPE + SecurityConfig rewiring created in green-acceptance
- [x] green-acceptance (created ProblemDetailAccessDeniedHandler + ErrorConstants.ACCESS_DENIED_TYPE, rewired SecurityConfig, removed @Disabled — GREEN, full suite 34 pass)
- [x] refactor (strengthened .claude/rules/coding-rules.md Error Handling: all errors incl. security-filter/framework -> ProblemDetail)
