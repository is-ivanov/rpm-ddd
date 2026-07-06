# Task 138: 401 UnauthorizedEntryPoint ProblemDetail -- Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: 401 unauthenticated-entry returns RFC-9457 ProblemDetail (issue #138)
Pure security-infrastructure fix (the `AuthenticationEntryPoint` in `SecurityConfig`). No
domain/usecase logic, so `red-usecase` / `green-usecase` are `[S]`. Twin of #130 / Task 13
(`ProblemDetailAccessDeniedHandler`). Full-stack journey: no-impact (backend error-shape only).
- [ ] red-acceptance (security: unauthenticated GET to a protected /api/** -> 401 body is RFC-9457 ProblemDetail; tag #138)
- [ ] design (custom ProblemDetailAuthenticationEntryPoint — mirror decisions/security-problemdetail-decision.md from Task 13)
- [S] red-usecase (no usecase/domain change — infrastructure-only)
- [S] green-usecase
- [ ] adapters-discovery (security entry-point adapter in SecurityConfig)
- [ ] green-acceptance
