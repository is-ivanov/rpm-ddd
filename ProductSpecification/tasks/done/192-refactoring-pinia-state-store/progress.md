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
- [x] refactor (logout via store) → `UserMenu` logout → `store.logout()` + `router.push('/login')`, drop `globalThis.location.reload()`; added store `logout()` action (calls logout api → `reset()`) + store unit test (100% coverage); provided a memory router to `home.smoke` to satisfy `useRouter()` → commit
- [x] refactor (decouple transport + guard skeleton) → `apiFetch` 401 → `authStore.reset()` (dropped `@/router` import/push); `unauthorized-redirect.logic` split into `isUnauthorized(status)` (transport) + `shouldRedirectToLogin(requiresAuth, isAuthenticated)` (guard); added `router.beforeEach` skeleton honoring `meta.requiresAuth` (no route opts in yet, `RouteMeta` augmented). Updated vitest (`fetch.api`, `unauthorized-redirect.logic`); login.api test gets active Pinia (login 401 now hits the reset path). User decisions: (Q1=A) #162 transport auto-redirect removed by design — deleted obsolete e2e `unauthorized-redirect.spec.ts` (401-on-/activate now renders the Link Expired view, already covered by the 422 expired-token e2e) + dropped its dead `givenSessionExpired` stub; (Q2) logout → `router.push('/')` (welcome), corrected `UserMenu` + ADR → commit
- [x] refactor (fix dependency inversion: session types/client → `app/`) → moved (git mv, history preserved) `features/home/logic/types.ts`→`app/logic/current-user.types.ts` (`AuthenticatedUser`/`CurrentUserResult`), `current-user.api.ts`→`app/logic/`, `dashboard-user.logic.ts`→`app/logic/` (`buildDashboardUser`/`DashboardUser`), `home/schemas/current-user.schema.ts`→`app/schemas/`, and the two unit tests→`app/__tests__/`. Updated all importers (store + `fetch.api`/`auth.store` tests); `home/logic` & `home/schemas` now empty (removed). Dependency direction is `features/` → `app/` only; the shared store no longer imports a feature. Full vitest 53 passed + lint clean → commit
