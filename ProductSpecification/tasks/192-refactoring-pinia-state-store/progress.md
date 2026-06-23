# Task 192: Decide — Introduce Pinia State Store — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Decision — adopt Pinia (auth/session store) or keep local refs
- [x] design — DECIDED: adopt Pinia, minimal auth/session store (ADR `decisions/pinia-auth-store-decision.md`); trigger fired via Story 3 (#206)

### Step 2: Implementation — adopt Pinia auth/session store
- [x] red-frontend (auth store) → store unit test (Vitest + setActivePinia): initial state unauthenticated; `loadMe()` sets user on 200 / stays unauthenticated on 401; `reset()` clears → /test-review → /refactor → commit
- [x] green-frontend (auth store) → `createPinia()` in `main.ts` (pinia dep added in RED), implement `app/stores/auth.store.ts` (state `currentUser`; getters `isAuthenticated`/`dashboardUser`; actions `loadMe`/`reset`) → /refactor → /test-coverage frontend --focus (100% auth.store.ts) → commit. NOTE: `loading`/`logout` deferred to the consuming refactor steps below (minimal GREEN)
- [x] refactor (migrate home subtree) → `HomePage` calls `store.loadMe()` + reads `store.dashboardUser` (local `loading` ref kept — presentational, per frontend-rules; ADR `loading` field NOT added to store); `UserMenu` reads `store.dashboardUser` via `storeToRefs`; dropped the 4-level prop chain (`DashboardShell`/`DashboardTopBar` no longer pass user props); component + vitest suite green → commit
- [~] refactor (logout via store) → `UserMenu` logout → `store.logout()` + `router.push('/login')`, drop `globalThis.location.reload()`; verify/adjust existing logout tests → /refactor → commit
- [ ] refactor (decouple transport + guard skeleton) → `apiFetch` 401 → `authStore.reset()` (drop the `@/router` import/push); add `beforeEach` guard honoring `meta.requiresAuth` (no route opts in yet); update `unauthorized-redirect` tests → /refactor → commit
- [ ] refactor (fix dependency inversion: session types/client → `app/`) → the shared layer (`app/stores/auth.store.ts`) must not import from a feature; move the session concerns out of `features/home/logic/` into `app/` — `AuthenticatedUser`/`CurrentUserResult` (types), `fetchCurrentUser` + `current-user.schema` (api client), and the dashboard view-model (`buildDashboardUser`/`DashboardUser`) the store getter depends on. Update all importers (store, `UserMenu`, tests) and their `__tests__` locations; direction becomes `features/` → `app/` only. Run full vitest + lint → /refactor → commit
