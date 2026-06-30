# Task 250: API clients use raw fetch() bypassing 401 session-reset; UsersPage silently swallows errors

Type: bug
Issue: #250

## Problem

Two API clients bypass the centralized 401-session-reset logic in `fetch.api.ts` by using
raw `fetch()` instead of `apiFetch()`:

1. **`admin-users.api.ts`** — uses `fetch(apiUrl(path), {...})` instead of
   `apiFetch(path, {...})`. On 401, `resetSessionWhenUnauthorized()` never fires, so the
   auth store retains stale state. The calling component (`UsersPage.vue`) has a
   `try/finally` without `catch` — the error is silently swallowed and the grid renders
   empty with no user feedback.

2. **`current-user.api.ts`** — same raw `fetch()` pattern. Handles 401 explicitly
   (returns `{ authenticated: false }`), but bypasses the unified
   `resetSessionWhenUnauthorized` path, creating a parallel 401-handling branch.

## Solution

1. Replace raw `fetch()` with `apiFetch()` in `admin-users.api.ts` and
   `current-user.api.ts`
2. Add error state to `UsersPage.vue` (`error` ref + error message display with a
   retry button) — the page must no longer silently swallow a failed load
3. Verify the existing `apiFetch()` 401-session-reset path activates correctly

## Design decision — error state targets *recoverable* failures, not 401

The user-observed trigger was a 401 (backend redeploy → dead session). However, the
correct UX for a 401 is **redirect to /login**, which is owned by **Task #251** (the
reactive `isAuthenticated` watcher). A retry button is meaningless on a 401 — re-fetching
just 401s again.

Therefore #250's error state is for **recoverable failures** (5xx, network errors, malformed
body) where **Retry is genuinely useful**, and the E2E test triggers it with a **500**, not
a 401. The core defect #250 fixes is the *silent swallow* (`try/finally` with no `catch`),
which hides every error class — exercised here via a 500. The 401-specific redirect is
deferred to #251; pinning "401 → error state" here would directly collide with #251.

## Affected Layers

Frontend only.

## Full-stack journey verdict

**no-impact.** The fix adds a recoverable-error UI surface (load-failure panel + Retry)
to the Users grid — an edge case, not the critical user-lifecycle spine. Per the
full-stack-journey policy, edge cases stay in the fast mocked suite (covered here by
`users-grid-error.spec.ts`), not the nightly real-stack journey. The 401→login redirect
that *would* touch the lifecycle path is Task #251, not this task.

## Key Files

- `frontend/src/features/users/logic/admin-users.api.ts`
- `frontend/src/features/users/components/UsersPage.vue`
- `frontend/src/app/logic/current-user.api.ts`
- `frontend/src/app/logic/fetch.api.ts`

## Related

- **Enables: Task #251** — the global 401→redirect watcher depends on `authStore.reset()`
  actually firing on 401, which requires `apiFetch()` to be the single 401-handling path

## Reproduction

1. Log in to the application
2. Restart the backend (simulating a deploy — new in-memory session store)
3. Navigate to /users — `GET /api/admin/users` returns 401
4. Observe: loading spinner disappears, grid renders empty, no error message shown
