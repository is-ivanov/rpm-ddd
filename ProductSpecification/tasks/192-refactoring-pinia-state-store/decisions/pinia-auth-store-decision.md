# Decision: Adopt Pinia — minimal auth/session store

**Date**: 2026-06-23 **Scenarios**: Task 192 (triggered by Story 3 / #206; linked to Story 1 `improvements.md` I1)

Task #192 deferred the state-layer decision until the "first real shared/session state" appeared.
Story 3 (#206 — home page, dashboard shell, user menu, auth navigation) is that trigger: it added a
genuine logged-in state and three concrete symptoms of having no store.

## What Story 3 changed (evidence the trigger fired)

- `GET /api/auth/me` is now called — `features/home/logic/current-user.api.ts` → `HomePage.vue`
  (`onMounted`). This closes the I1 gap ("`/me` never called, stays on `/login`").
- Successful login now navigates: `LoginPage.vue` → `router.push('/')`.
- Real logged-in state exists: `WelcomeView` (guest) vs `DashboardShell` (authenticated).

### Three symptoms of no store (new since the 4-screen baseline)

1. **Prop-drilling of the current user 3-4 levels**: `HomePage` → `DashboardShell` →
   `DashboardTopBar` → `UserMenu`. One source, threaded through the whole subtree as props.
2. **Logout via `globalThis.location.reload()`** (`UserMenu.vue`) — a full page reload only because
   there is no reactive store to clear.
3. **Two divergent 401 strategies**: transport redirect in `app/logic/fetch.api.ts` (for `apiFetch`)
   **and** a separate "swallow 401 → `{ authenticated: false }`" in `fetchCurrentUser()` (which
   bypasses `apiFetch`). This is exactly the senior's "redirect wired into transport" smell, now
   duplicated.

| Rejected | Why |
|----------|-----|
| Keep local `ref`s | Forces the three symptoms above; every new authenticated screen re-wires prop-drilling and re-fetches `/me`, raising future migration cost |
| Broad store now (auth + app-shell + profile/patients) | Over-engineering at 4 screens; profile/patient state has no consumer yet — build it when those features land |
| EventBus | Vue 3 anti-pattern (senior's remark); a reactive store covers its role |

**Chosen**: Pinia, scoped to **one** `auth/session` store — single source of truth for the current
user. Removes all three symptoms with a small migration surface (one subtree).

## Model

- **New dependency**: `pinia` in `frontend/package.json`; `createPinia()` installed in `main.ts`
  (before `router`, since the guard reads the store).
- **Store location** (convention, to add to `frontend-rules.md` — mirrors the zod schema split from
  Task #191): cross-feature/session state → `src/app/stores/*.store.ts`. First entry:
  `app/stores/auth.store.ts`. Feature-local stores (none yet) would live in
  `features/{feature}/stores/`.
- **Store shape** (`auth.store.ts`):
  - state: `currentUser: AuthenticatedUser | null`, `loading: boolean`
  - getters: `isAuthenticated`, `dashboardUser` (reuse `buildDashboardUser`)
  - actions: `loadMe()` (calls `fetchCurrentUser`, sets user or stays unauthenticated on 401),
    `logout()` (calls the `logout` api then `reset()`), `reset()` (clears `currentUser`).
- **Humble Object preserved**: the store orchestrates existing `.api.ts`/`.logic.ts` (it does not
  inline `fetch` or validation). API clients and `buildDashboardUser` stay the logic layer.
- **Consumers**: `HomePage` calls `store.loadMe()` and reads `store.dashboardUser`; `DashboardShell`
  /`DashboardTopBar`/`UserMenu` read the store (or a single prop from `HomePage`) instead of the
  4-level prop chain. `UserMenu` logout → `store.logout()` + `router.push('/login')`, dropping
  `location.reload()`.

### Transport / router decoupling — corrected scope

The senior's "move the 401 redirect from transport to a reactive guard" is imprecise as a literal
move: a fetch-time 401 (session died mid-request) and a nav-time guard (navigating to a protected
route) are **different moments**, not substitutes. The clean target:

- **Transport** (`fetch.api.ts` `apiFetch`): on 401, call `authStore.reset()` instead of importing
  `@/router` and pushing — removes the transport→router coupling and makes the redirect *reactive*.
- **Guard** (`router/index.ts` `beforeEach`): redirect to `/login` when a `requiresAuth` route is
  entered while `!store.isAuthenticated`. The home page stays public by design (Story 3 renders
  `WelcomeView` for guests), so no route is `requiresAuth` **yet** — the guard is the documented home
  for the first protected route/data screen, installed now so future screens opt in via route meta.

## Edge Cases

| Case | Behavior |
|------|----------|
| `/me` returns 401 (guest) | `loadMe()` leaves `currentUser` null; `HomePage` renders `WelcomeView`. No redirect — `/` is public. |
| Session dies during an `apiFetch` call | Transport calls `authStore.reset()`; reactive state flips `HomePage` to `WelcomeView` (and the guard would redirect once a `requiresAuth` route exists). |
| Logout | `store.logout()` → POST `/api/auth/logout` (server clears the cookie) → `reset()` → `router.push('/login')`. No full-page reload. |
| `/me` returns a schema-invalid payload | `currentUserResponseSchema.parse` throws (Task #191 contract) — surfaced as an error, not a fake unauthenticated state. |
| Multiple tabs / store not persisted | Store is in-memory per tab; truth is the server cookie + `loadMe()` on app start. No `localStorage` persistence (no requirement; avoids stale-session bugs). |
| File-size limit | `auth.store.ts` stays well under 200 lines (focused on session only). |

## Out of scope (revisit when a protected data screen lands)

- A populated `requiresAuth` guard (no protected route exists yet).
- Profile / patient state in the store.
- `localStorage`/persisted-state plugin.
