# Task 192: Decide — Introduce Pinia State Store — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Decision — adopt Pinia (auth/session store) or keep local refs
- [x] design — DECIDED: adopt Pinia, minimal auth/session store (ADR `decisions/pinia-auth-store-decision.md`); trigger fired via Story 3 (#206)

### Step 2: Implementation — adopt Pinia auth/session store
- [~] red-frontend (auth store) → store unit test (Vitest + setActivePinia): initial state unauthenticated; `loadMe()` sets user on 200 / stays unauthenticated on 401; `reset()` clears → /test-review → /refactor → commit
- [ ] green-frontend (auth store) → add `pinia` dep, `createPinia()` in `main.ts`, implement `app/stores/auth.store.ts` (state `currentUser`/`loading`; getters `isAuthenticated`/`dashboardUser`; actions `loadMe`/`logout`/`reset`) → /refactor → /test-coverage frontend --focus → commit
- [ ] refactor (migrate home subtree) → `HomePage` calls `store.loadMe()` + reads `store.dashboardUser`; drop the 4-level prop chain through `DashboardShell`/`DashboardTopBar`/`UserMenu`; keep existing component + playwright tests green → /refactor → commit
- [ ] refactor (logout via store) → `UserMenu` logout → `store.logout()` + `router.push('/login')`, drop `globalThis.location.reload()`; verify/adjust existing logout tests → /refactor → commit
- [ ] refactor (decouple transport + guard skeleton) → `apiFetch` 401 → `authStore.reset()` (drop the `@/router` import/push); add `beforeEach` guard honoring `meta.requiresAuth` (no route opts in yet); update `unauthorized-redirect` tests → /refactor → commit
