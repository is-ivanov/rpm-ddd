# Decision: Logout as a web/security adapter method

**Date**: 2026-05-29 **Scenarios**: 6.1

Logout invalidates an HTTP session and clears the security context — no business rule is involved, so the question is only where the session-teardown lives.

| Rejected | Why |
|----------|-----|
| Spring Security `http.logout()` filter | Splits the auth flow across two mechanisms; the project manages the `SecurityContext` manually in `AuthResource` (see `login`), so a filter-based logout would be inconsistent. |
| Application service (usecase) for logout | No domain logic to orchestrate; session/context teardown is a pure infrastructure concern. |

**Chosen**: A thin `@PostMapping("/logout")` method on `AuthResource` that delegates to `SecurityContextLogoutHandler` to invalidate the `HttpSession` and clear the holder, then returns 200. Symmetric with the manual `SecurityContext` handling already used by `login`.

## Model

- `AuthResource.logout(HttpServletRequest, HttpServletResponse)` — new web adapter method. No usecase, no domain, no response DTO.
- `SecurityConfig` — endpoint authorization is now an **allow-list (deny-by-default)**: only the public endpoints (`GET /api/auth/csrf`, `POST /api/auth/login`, `GET`/`POST /api/auth/activate`) are `permitAll`; `/api/**` is `authenticated()`; `anyRequest()` is `denyAll()`. So `/api/auth/me` and `/api/auth/logout` — and any future auth endpoint — require authentication by default. An unauthenticated request after logout yields 401 via `UnauthorizedEntryPoint` instead of reaching the controller with a null principal (500). Replaces the original `permitAll` on `/api/auth/**`, a latent gap that Scenario 5.1's happy-path-only test never exercised. Principle encoded in `.claude/rules/coding-rules.md` → Security.
- No new ports.

## Edge Cases

| Case | Behavior |
|------|----------|
| Request with no active session/auth | `SecurityContextLogoutHandler` is a no-op on a null authentication; endpoint still returns 200. |
| Follow-up request with the invalidated `JSESSIONID` | `HttpSessionSecurityContextRepository` finds no context → `/api/auth/me` returns 401. |
