# Story 3: Home page — Improvements Backlog

Deferred work, decided at the Story Completion Gate (2026-06-23). Each item is
`Open` until a resolving task/PR moves it to `Done`. Architecture is deferred:
revisit these after the base story ships — design solutions then (an ADR, or
promote into a dedicated improvement story).

## Open

### I1 — Loading indicator while the current user is being resolved
- **Source**: `tests/extended/02_UI_Tests_Extended.md` §1.1.
- **Observed**: no E2E test pins the loading state shown before `/api/auth/me` resolves.
- **Current code**: already implemented — `HomePage.vue` renders `AppLoading` while `loading` is true; behaviour exists but is untested.
- **Why deferred**: purely presentational and already built; a dedicated E2E adds little. Gate recommendation accepted.
- **Scope options**: add a Playwright scenario with a delayed `/me` mock asserting the spinner, then welcome/dashboard.

### I2 — Session expiry while viewing the dashboard returns to the welcome page
- **Source**: `tests/extended/02_UI_Tests_Extended.md` §2.1.
- **Observed**: if the session expires while the dashboard is open, the user is not actively returned to the welcome page.
- **Current code**: not implemented. `HomePage` fetches `/me` only on mount; the shared 401→`/login` redirect (`unauthorized-redirect.logic.ts`) fires on protected API calls, but a static dashboard issues none after load.
- **Why deferred**: needs design — how/when expiry is detected on an idle dashboard (polling, on-focus re-check, or next-action 401). Not a quick promote.
- **Scope options**: re-check `/me` on window focus/visibility; or a lightweight session-validity poll; or rely on the next user action's 401 redirect.

### I3 — Clicking outside the open user menu closes it
- **Source**: `tests/extended/02_UI_Tests_Extended.md` §3.1.
- **Observed**: the user menu only toggles via the avatar; an outside click does not close it.
- **Current code**: not implemented — `UserMenu.vue` toggles `open` on avatar click only; no outside-click handler.
- **Why deferred**: UX polish, not part of the core auth flow.
- **Scope options**: add an outside-click directive/listener that clears `open`.

### I4 — A single-word name produces a single initial
- **Source**: `tests/extended/02_UI_Tests_Extended.md` §4.1.
- **Observed**: initials derivation is unverified for a name with no last name.
- **Current code**: `dashboard-user.logic.ts` `buildDashboardUser` derives initials from firstName+lastName; the single-word case is not covered by a test.
- **Why deferred**: edge case; depends on whether the backend can emit a single-word name (current `/me` always provides firstName + lastName).
- **Scope options**: add a `dashboard-user.logic` unit test for single-word names; harden the derivation if the backend can omit a last name.

### I5 — XSS in profile name is rendered as inert text
- **Source**: `tests/extended/05_Security_Tests_Extended.md` §1.1.
- **Observed**: no test pins that a profile name containing markup renders as literal text.
- **Current code**: safe by construction — Vue auto-escapes `{{ }}` text interpolation; the name is self-scoped (a user sees only their own profile).
- **Why deferred**: defence-in-depth only; the framework already guarantees escaping (noted in the extended file itself).
- **Scope options**: add a Playwright defence-in-depth check that a `<script>`-laden name renders as text and executes nothing.

## Done

(none yet)
