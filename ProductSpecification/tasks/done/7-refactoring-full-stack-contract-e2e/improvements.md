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

### I2 — `frontend/acceptance/**` has no proper static-analysis configuration

- **Observed**:
  - IntelliJ reports "unnecessary non-null assertion" on `match![0]` / `token!`
    (`mailpit.statements.ts`) and on `session!` / `csrf!` (×7,
    `real-auth-backend.statements.ts`) — all REQUIRED under the project's
    `"strict": true`; the IDE flags them via a non-strict fallback.
  - SonarLint S2068 ("potentially hard-coded password") fires on
    `password: 'Fullstack@123'` (`real-auth-backend.statements.ts`) — an
    intentional test password for a synthetic journey user (the test sets it
    during activation and reuses it to log in), not a secret.
- **Spec context**: `frontend/tsconfig.json` has a single config with
  `"strict": true` but `"include": ["src/**/*.ts", ...]` — the entire
  `frontend/acceptance/**` tree (Playwright specs + Statements) is not in any
  tsconfig `include`, and no inspection scope marks it as test code. Playwright
  transpiles via esbuild (no type-check), so the acceptance suite has never been
  type-checked.
- **Current code state**: the build's `vue-tsc --noEmit` (driven by tsconfig)
  silently skips acceptance files; only the IDE checks them, with strictNullChecks
  effectively off and security heuristics applied as if to production code — hence
  the false-positive `!` warnings, the S2068 noise, and zero real type coverage.
- **Scope options**: (a) add `frontend/acceptance/**` to a strict tsconfig
  `include` (or a dedicated `tsconfig.acceptance.json` via project references) and
  wire a type-check into the lint/CI gate — auto-clears the false-positive `!`
  warnings and gives real coverage; (b) mark the acceptance tree as test code for
  the IDE/SonarLint inspection scope so secret-detection heuristics (S2068) don't
  fire on intentional test fixtures. Expect (a) to surface other latent type
  issues across the existing acceptance tree (handle as its own task). Until then
  the `!` assertions stay (correct under strict) and the test password stays; do
  NOT remove them.
