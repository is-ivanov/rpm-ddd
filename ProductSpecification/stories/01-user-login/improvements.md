# Story 1 (User Login) — Improvements Backlog

Running list of **enhancements** to the login / Story-1 flow found during QA.

## How this list works

- Items here are **improvements** — under-specified or missing-by-design behaviour, not regressions.
- **Real bugs** (broken or regressed behaviour) do **not** go here — file them as separate `bug`
  tasks with a backing GitHub issue (same pattern as Tasks 12/13/14 → #129/#130/#131).
- **Architecture is deferred:** finish Story 1 in its current form first, then revisit this list and
  design solutions (ADRs, or promote this file into its own improvement story).
- Add items as `Open`; move to `Done` (with the resolving task/PR) once addressed.

## Open

### I1 — No post-login navigation / logged-in state (found 2026-06-08)

**Observed:** after a successful `POST /api/auth/login` (200 OK) the SPA stays on `/login`.
`LoginPage.submitLogin()` does nothing on success — only the `catch` branch runs (error banner);
there is no `router.push` and no `/me` call.

**Spec context:** `01_UserLogin.md` Flow §4 ("Frontend calls GET /api/auth/me to display current
user info") and Screen States ("Logged-in state: user info displayed via /me endpoint") imply a
logged-in state, but it was never turned into a UI scenario (`tests/02_UI_Tests.md` §6 Navigation has
only **6.1** activation→login) nor implemented. No concrete destination page is named.

**Current state of the code:**
- `/` route → `HomePage.vue` — a placeholder ("RPM / Remote Patient Monitoring"); nothing routes to
  it after login, and it does not call `/me`.
- No auth-based router guard (`/` is public; `SecurityConfig` permits `GET /`).
- Backend `/api/auth/me` exists (Story 1 backend Scenario 5.1) but the frontend never calls it.

**Scope options to decide at design time:**
- *Minimal:* `router.push('/')` on successful login.
- *Spec-aligned:* fetch `GET /api/auth/me` → render a real logged-in state (beginning of an
  authenticated app shell).
- *Likely also needed:* auth router guard, redirect-after-login back to the originally requested URL,
  and a defined landing for unauthenticated users.

## Done

_(none yet)_
