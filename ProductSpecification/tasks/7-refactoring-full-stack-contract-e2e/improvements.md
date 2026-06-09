# Task 7 — Improvements Backlog

Findings surfaced while building the full-stack E2E journey. These are NOT bugs
(the behavior was never specified-and-built that way) — see workflow.md
"Improvements vs Bugs". Architecture is deferred until the base task is done.

## Open

### I1 — Login page exposes no post-success UI signal

- **Observed**: On a successful UI login, `LoginPage.vue` does nothing observable —
  no redirect, no success screen, no field reset (fields are only cleared on
  error). The session is established correctly (JSESSIONID Set-Cookie lands), but
  the page stays on the Sign In form with the entered credentials still visible.
- **Spec context**: Story 1 login was specified as "authenticate + set session
  cookie"; a post-login destination (dashboard/home) is not yet a story. The
  full-stack E2E journey (Task 7) was the first test to drive the real login
  end-to-end (the mocked UI tier never asserted a live cookie).
- **Current code state**: `submitLogin()` awaits `login(...)` and, on success,
  returns with no navigation. The full-stack journey works because
  `RealAuthBackendStatements.assertSessionCookieIsSet()` polls the cookie jar
  (async side-effect wait, no sleep) instead of relying on a UI signal.
- **Scope options**: (a) add a post-login redirect to a landing/dashboard route
  once that page exists; (b) show a transient success state. Revisit when the
  post-login destination becomes a story. Until then the polling cookie wait is
  the correct E2E approach.
