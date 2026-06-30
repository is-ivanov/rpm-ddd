## align-design (2026-06-23)

**Decision:** Logout (`UserMenu.vue` `handleLogout()`) calls `logout()` then does a full `window.location.reload()` instead of client-side route navigation; loading state is disable-only (`loggingOut` ref + `:disabled`), no spinner.
**Why:** User-chosen to honor the DSL by forcing a fresh `GET /api/auth/me` → 401 → welcome on reload, rather than trusting client-side auth state to update reactively; a spinner would never be visible since the page navigates away immediately on success.
**Where applied:** `UserMenu.vue` `handleLogout()`.
