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

**Related:** the state-layer decision for this lives in Task #192 (Pinia store) — the 401 redirect is
currently wired into the transport (`fetch.api.ts`) for lack of a reactive auth store.

### I2 — Load/performance baselines deferred from MVP (decided 2026-06-15)

**Observed:** Story 1 `tests/03_Load_Tests.md` defines three performance scenarios that were
deferred at first-stage scope review — none implemented:
- **3.1** Login response time under 200ms
- **4.1** 50 concurrent login requests, max response time under 500ms
- **5.1** Activation token validation response time under 200ms

**Why deferred:** premature optimization for an MVP — the thresholds (200ms/500ms) are
hardware-coupled to CI and inherently flaky, and 4.1 needs a load-test harness
(JMeter/Gatling/virtual threads) the project doesn't yet have. Value appears only once there is
production traffic and an agreed SLA.

**Current state of the code:** no load-test harness, no timing assertions; functional login /
activation paths fully implemented and covered by the Backend + Security scenarios.

**Scope options to decide at design time:** introduce a dedicated load-test harness and revisit the
thresholds against real CI/prod hardware, or promote into a "Login hardening / performance" story.
Marked `[S] deferred` in `progress.md` (Load Scenarios).

### I3 — DB-failure resilience deferred from MVP (decided 2026-06-15)

**Observed:** Story 1 `tests/04_Infrastructure_Tests.md` defines two resilience scenarios deferred
at first-stage scope review — none implemented:
- **4.1** Database unavailable during login returns 500
- **5.1** Database recovery allows login after outage

**Why deferred:** exercising these requires a stateful infra harness that takes the DB down and back
up mid-test — expensive to build and maintain, low value before production traffic. The
"unavailable → 500 (no leaked stack trace)" guarantee is worth having eventually but belongs in a
hardening phase, not the MVP.

**Current state of the code:** no DB-outage simulation in tests; the global exception handler /
error-handling config governs error responses for the happy paths.

**Scope options to decide at design time:** build a DB-toggle test harness (Testcontainers
pause/resume or a failing DataSource proxy) and assert graceful 500 + RFC 9457 Problem Detail, or
fold into a "Login hardening / resilience" story. Marked `[S] deferred` in `progress.md`
(Infrastructure Scenarios).

### I4 — Deferred extended UI cases never promoted (found 2026-06-20, FE audit)

**Observed:** the senior FE audit (`audit.md`) flagged several auth-form gaps that turned out to be
**already specified** in `tests/extended/02_UI_Tests_Extended.md` (header *"Implement after core tests
pass"*) but never tracked in `progress.md` and never promoted:

- **passwords do not match** — `confirmPassword` is decorative: `submitActivation` sends only
  `password`, no comparison (`ActivationPage.vue`).
- **password strength indicator updating in real-time** — `PASSWORD_RULES` always render a green
  `Check`; no client validation.
- **login loading state during submission** — no submitting state → double-submit possible.
- **error banner dismiss button** — plus the dead "Request a new activation email" link
  (`href="#"`) in `LoginErrorBanner.vue`.

**Spec context:** all four exist in the Story 1 extended UI test file; deferred by design, orphaned
by the process gap now tracked as #187 (extended cases never enter the pipeline).

**Current state of the code:** built only to the core happy-path (confirm field visible but unused,
rules always green, no loading state). Backend rejects bad passwords correctly; the gap is FE-only.

**Scope options to decide at design time:** promote into Story 1 UI scenarios (red-playwright /
red-frontend cycle) or keep deferred here. Tracked as enhancement **#189**. The fake-success-on-4xx
half of the activation finding is a **real bug** (never specified) — split out to Task #188.

## Done

_(none yet)_
