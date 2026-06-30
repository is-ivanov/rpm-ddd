# Story 3 — Carryover

Enduring codebase quirks and decisions promoted from completed scenarios. Read on resume; verify against current code before relying on them.

## Decision: login redirect is a fixed path, no return-URL
**Decision:** Successful login always redirects to `/` via `router.push('/')` — there is no return-URL/deep-link-after-login feature.
**Why:** Scenario 4.2 only required navigation to the home page; a constant target failed the trivial-logic gate for a dedicated logic function.
**Where applied:** `LoginPage.vue` submit handler.
**From:** scenario 4.2 (4-2-login-redirect)

## Quirk: logout forces a full page reload, not SPA navigation
**Quirk:** Logout does not route client-side back to welcome — it calls `logout()` then `window.location.reload()`, which re-fetches `/api/auth/me` (now 401) to redraw the welcome view. Any client-side reactive auth state is discarded on logout, not transitioned.
**Where:** `UserMenu.vue` `handleLogout()`.
**From:** scenario 4.3 (4-3-logout-full-reload)
