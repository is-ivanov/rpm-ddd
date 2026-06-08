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
- [~] design (how to make AccessDeniedHandler / AuthenticationEntryPoint emit ProblemDetail)
- [S] red-usecase (no usecase/domain change — infrastructure-only)
- [S] green-usecase
- [ ] adapters-discovery (security error-handler adapters in SecurityConfig)
- [ ] green-acceptance
- [ ] refactor (strengthen .claude/rules/coding-rules.md Error Handling: all errors incl. security-filter/framework -> ProblemDetail)
