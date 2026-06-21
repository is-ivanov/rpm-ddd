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

### I5 — Dead "Request a new activation email" link needs a resend-activation feature (found 2026-06-20)

**Issue:** #199 (opened 2026-06-21, split out of #189 on closure).

**Observed:** the inactive-account error banner (`LoginErrorBanner.vue`, rendered in Scenario 3.2)
contains a "Request a new activation email" link with `href="#"` — it goes nowhere.

**Spec context:** Scenario 3.2 *requires* the link to be present ("the error banner contains a link to
request a new activation email"), so the link must NOT be removed. Making it functional requires a
**resend-activation** capability (a backend endpoint to re-issue an activation email + a frontend
flow) that does not exist in Story 1. This is therefore a missing-by-design enhancement, not a UI
dismiss-button concern — it was split out of I4 when the four extended UI cases were promoted.

**Current state of the code:** link rendered with `href="#"`; no resend endpoint, no resend flow.

**Scope options to decide at design time:** add a resend-activation backend endpoint + wire the link
to a real flow (likely its own small story or a Story 1 follow-up), or convert the link to a
router/action target once such a flow exists.

## Done

### I6 — Activation submit button has no loading state; extract a shared LoadingButton (found 2026-06-21) — DONE 2026-06-21 (promoted)

**Resolved by:** promoted into Story 1 core UI scenario **4.4** "Activation page shows loading state
during submission" (`tests/02_UI_Tests.md` + full frontend TDD cycle in `progress.md`, scheduled last
after the other promoted scenarios). Its align-design step must **extract a shared `LoadingButton`**
(props: label, loading label, loading flag, disabled, test-ids) into the shared UI directory and
migrate BOTH the §2.2 login button and the activate button onto it (§2.2 login tests stay green).

**The original finding:** loading state is a cross-cutting UX requirement for *every* button that fires
a network request, not a one-off login feature. Among the backend-calling auth controls, only
`ActivationPage.vue`'s activate button (`activateAccount()` POST) still had NO loading state →
double-submit possible. This is the **activation half** of the #189 finding ("no submitting state →
double-submit possible (`LoginPage`, `ActivationPage`)"); the login half was closed by Story 1 §2.2.
Under-specified by design (the extended UI spec had a loading scenario only for login), so it was an
improvement, not a bug. The generalized rule now lives in `.claude/rules/frontend-rules.md` → "Async
Action Buttons (Loading State)": every backend-calling control must reflect in-flight state, and
adding one without a loading state requires user confirmation.

### I4 — Deferred extended UI cases never promoted (found 2026-06-20, FE audit) — DONE 2026-06-20

**Resolved by:** promoted into Story 1 core UI scenarios (issue **#189**) — `tests/02_UI_Tests.md`
gained scenarios **2.2** (login loading state), **3.3** (error banner dismiss), **4.2** (real-time
password strength), **4.3** (passwords do not match), each tracked with the full red-playwright /
red-frontend TDD cycle in `progress.md`. #191 (zod client validation) being merged unblocked 4.2/4.3.

The original finding: the senior FE audit (`ProductSpecification/audits/2026-06-20-frontend-audit.md`)
flagged auth-form gaps already specified in `tests/extended/02_UI_Tests_Extended.md` (*"Implement
after core tests pass"*) but never tracked in `progress.md`:

- **passwords do not match** — `confirmPassword` decorative; `submitActivation` sends only `password`.
- **password strength indicator updating in real-time** — `PASSWORD_RULES` always render green `Check`.
- **login loading state during submission** — no submitting state → double-submit possible.
- **error banner dismiss button** — dismiss not wired (the dead resend link split out to **I5**).

Orphaned by the process gap tracked as #187 (extended cases never enter the pipeline). The
fake-success-on-4xx half of the activation finding was a **real bug** (never specified) — handled in
Task #188.
