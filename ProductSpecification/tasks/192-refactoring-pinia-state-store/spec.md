# Task 192: Decide — Introduce Pinia State Store

Type: refactoring
Issue: #192

> **Discussion task.** Decide *whether* to apply before writing any code. Step 1 produces the
> decision (and an ADR if adopted). Linked to `stories/01-user-login/improvements.md` **I1** and
> Story 3 (home page).

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

_To be filled in Step 1._ If adopted: scope = auth/session store + router guard; coordinate with I1
and Story 3. Then move the `fetch.api.ts` redirect to the guard.

## Key Files

- `frontend/src/app/logic/fetch.api.ts`, `unauthorized-redirect.logic.ts` (transport-coupled redirect)
- `frontend/src/router/index.ts` (where a guard would live)
- `stories/01-user-login/improvements.md` (I1)
