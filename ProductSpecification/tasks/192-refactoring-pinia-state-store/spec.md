# Task 192: Decide — Introduce Pinia State Store

Type: refactoring
Issue: #192

> **Discussion task.** Decide *whether* to apply before writing any code. Step 1 produces the
> decision (and an ADR if adopted). Linked to `stories/01-user-login/improvements.md` **I1** and
> Story 3 (home page).

## Dependencies

- **Linked to improvements `I1`** (no post-login / logged-in state) and **Story 3** (home page) —
  the first real shared/session state is the trigger; coordinate the decision with whoever picks up
  I1 / Story 3.
- **Touches #162/Task 8 output** (done): the 401→login redirect in `fetch.api.ts` would move from
  the transport into a reactive router guard if Pinia is adopted.
- No hard blocker; independent of #190/#191/#193. Best decided alongside Story 3 planning.

## Problem

The FE audit notes there is no state layer: all state is local `ref`s, cross-component coupling is
props/emits only. A concrete symptom: the 401→login redirect is wired directly into the transport
(`fetch.api.ts`, from #162/Task 8) for lack of a reactive auth store. Tolerable for 4 screens, but it
becomes a real defect with the first shared/session state (logged-in user, profile, patient data).

## Our current approach

- No store. State is component-local `ref`s; sharing via props/emits.
- Auth/session state is not modelled at all; `improvements.md` **I1** already flags "no post-login /
  logged-in state" and that `/me` is never called.
- 401 redirect lives in the HTTP transport layer rather than a reactive guard.

## Constraints from `.claude` rules

- `frontend-rules.md`: Humble Object — pure logic in `.logic.ts`, HTTP in API clients, thin
  components; feature-based folders.
- `coding-rules.md` (backend, but the principle informs FE): no in-memory cross-instance state — a
  store is client-only UI state, not server state.
- File-size limit 200 lines; a store must stay focused (auth/session).

## Senior's remark

> "Нет слоя управления состоянием (Pinia/store)… отсутствие — корень костыля: редирект на 401 зашит
> прямо в транспорт вместо реактивного auth-стора + router guard. EventBus не нужен — в Vue 3 это
> анти-паттерн; его роль закрывает стор."

## Pros / Cons

**Adopt Pinia (auth/session store):**
- + Single source of truth for session; enables I1 (post-login state, `/me`, app shell).
- + Lets the 401 redirect move to a reactive guard, decoupling transport from the router.
- + Idiomatic Vue 3 (replaces the EventBus anti-pattern).
- − New dependency + structure decision; overkill if the app stays at a few stateless screens.

**Keep local refs:**
- + Simplest; fine for the current 4 screens.
- − Forces transport-coupled hacks; doesn't scale to shared state (profile/patients).

## Decision

**ADOPT Pinia — minimal auth/session store** (2026-06-23). See
`decisions/pinia-auth-store-decision.md`.

The trigger fired: Story 3 (#206) added a real logged-in state plus three symptoms of having no
store — (1) the current user is prop-drilled 3-4 levels (`HomePage` → `DashboardShell` →
`DashboardTopBar` → `UserMenu`), (2) logout uses `globalThis.location.reload()` for lack of a
reactive store to clear, (3) two divergent 401 strategies coexist (transport redirect in
`fetch.api.ts` vs. swallow-401 in `fetchCurrentUser`).

Scope = ONE `app/stores/auth.store.ts` (`currentUser`/`loading`, `isAuthenticated`/`dashboardUser`
getters, `loadMe`/`logout`/`reset` actions) → migrate the home subtree off prop-drilling → logout via
`store.reset()` → decouple `apiFetch` 401 from the router (call `authStore.reset()` reactively; add a
`requiresAuth` guard skeleton for the first future protected route). Broad/profile/patient state and
persistence are out of scope until a protected data screen lands. Closes the state-layer half of I1.

## Key Files

- `frontend/src/app/logic/fetch.api.ts`, `unauthorized-redirect.logic.ts` (transport-coupled redirect)
- `frontend/src/router/index.ts` (where a guard would live)
- `stories/01-user-login/improvements.md` (I1)
