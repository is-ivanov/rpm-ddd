# Task 251: No reactive redirect to /login when session is lost mid-page

Type: bug
Issue: #251

## Problem

When an API call returns 401 mid-page (session timeout, deploy, cookie expiry), the auth
store is reset via `resetSessionWhenUnauthorized` in `fetch.api.ts`, but the user stays
on the current page with no redirect to `/login`.

The router guard (`beforeEach`) only checks authentication on navigation — it does not
react to auth state changes while the user is already on a page.

## Current State

- `fetch.api.ts` already calls `authStore.reset()` on 401 via
  `resetSessionWhenUnauthorized` — the session IS cleared
- Router (`index.ts:56-59`) has `beforeEach` that reads `isAuthenticated` and redirects
  to `LOGIN_PATH` — but only on navigation
- No reactive watcher exists: when `isAuthenticated` transitions `true → false` mid-page,
  nothing happens

## Solution

1. Add a global watcher (in router setup or `App.vue`) that watches
   `authStore.isAuthenticated` and calls `router.push(LOGIN_PATH)` when it becomes
   `false`
2. Ensure the watcher only fires on a transition (authenticated → unauthenticated), not
   on initial load (where `isAuthenticated` starts as `false` before `loadMe()`
   completes)
3. Consider showing a notification before redirect: "Session expired; please log in
   again."

## Affected Layers

Frontend only.

## Key Files

- `frontend/src/router/index.ts` — add reactive watcher
- `frontend/src/app/stores/auth.store.ts` — expose `isAuthenticated` for watching

## Related

- **Depends on: Task #250** — `apiFetch()` must actually reach
  `resetSessionWhenUnauthorized`. Currently `admin-users.api.ts` bypasses it via raw
  `fetch()`.
- **Full chain:** raw fetch fix (#250) → `authStore.reset()` fires on 401 → watcher
  sees `isAuthenticated → false` → redirects to `/login`

## Reproduction

1. Log in to the application
2. Restart the backend (simulating a deploy)
3. Navigate to /users — `GET /api/admin/users` returns 401
4. Observe: after Task #250 fix, auth store resets, but user stays on /users page
   with no redirect to /login
