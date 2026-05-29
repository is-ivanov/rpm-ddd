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
- No new ports.

## Edge Cases

| Case | Behavior |
|------|----------|
| Request with no active session/auth | `SecurityContextLogoutHandler` is a no-op on a null authentication; endpoint still returns 200. |
| Follow-up request with the invalidated `JSESSIONID` | `HttpSessionSecurityContextRepository` finds no context → `/api/auth/me` returns 401. |
