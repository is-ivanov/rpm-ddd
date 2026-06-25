# Task 213: CI/CD pipeline maintenance

Type: refactoring
Issue: #213  <- task number IS the issue number; refactoring records it for traceability (tests are not tagged)

## Problem

Audit of `.github/workflows/` surfaced one real gap and several hardening opportunities.
All recent runs are green (nightly full-stack E2E has been `success` for 7+ days, ~2 min
each; only failure in the last 40 runs across all workflows was the already-resolved
Prettier `--check` fail on Task 210). This is **preventive** maintenance, not a reaction to
a current failure.

**Main gap — a red nightly run is silent.** `nightly-fullstack-e2e.yml` has no
failure-handling step. On `failure()` it only uploads `fullstack-backend.log`. If the
nightly full-stack journey breaks, nothing is auto-created — it is discoverable only via the
Actions tab (or GitHub's default email to the workflow file's last committer). There is no
automatic "investigate this" task/issue. The same silence applies to `checkstyle-updates.yml`.

**Naming + hidden coupling.** `build.yml` is named `Java CI with Maven`, but it runs backend
(Java) **plus** `frontend-build` (vitest), `frontend-e2e` (Playwright), `allure-report`, and
Pages deploy — not "just Java". It cannot be renamed in isolation: `deploy.yml` triggers via
`workflow_run: workflows: ["Java CI with Maven"]`. Renaming `build.yml` without updating
`deploy.yml` in the same change would **silently stop deploys**.

## Solution

A single maintenance pass over the workflow files. Steps are ordered most-valuable-first
(Step 1 = the user's priority), and each is independently shippable.

1. **Nightly failure → auto GitHub issue.** Add an `if: failure()` step to
   `nightly-fullstack-e2e.yml` that opens (or reopens/dedupes) an issue labelled
   `nightly-failure`, linking the failed run. Uses `GITHUB_TOKEN` — no external secret.
2. **Safe rename of `Java CI with Maven`** to a name covering the full stack
   (e.g. `CI` / `Build & Test`), updating `deploy.yml`'s `workflow_run.workflows` reference
   in the **same** commit so deploy keeps firing.
3. **`timeout-minutes`** on jobs — no job currently has one; a hung Playwright/e2e job can
   run to GitHub's 6h default.
4. **`concurrency`** on `build.yml` to cancel superseded PR runs (currently every push runs a
   full build+e2e in parallel with the previous).
5. **Least-privilege permissions** — `pages: write` / `id-token: write` are elevated
   workflow-wide in `build.yml`, but only `deploy-report` (main-only) needs them; scope to
   that job.
6. **Node version sync** — `22.13.0` is hardcoded in several places while `allure-report`
   uses `lts/*`; align/centralize.
7. **Nightly backend teardown** — `BACKEND_PID` is captured but never used; add a teardown
   step.

## Key Files

- `.github/workflows/nightly-fullstack-e2e.yml` — Steps 1, 3, 7
- `.github/workflows/build.yml` — Steps 2, 3, 4, 5, 6
- `.github/workflows/deploy.yml` — Step 2 (coupled rename)
- `.github/workflows/code-quality.yml` — Steps 3, 6

## Notes

- **Pure CI/infra (YAML) work — the standard Java/TS TDD cycle does not apply.** There is no
  unit under test; changes are verified by `actionlint` (syntax/lint) plus a manual
  `workflow_dispatch` where behaviour must be observed (e.g. the nightly auto-issue path).
  Progress steps below are therefore implement + verify, not red/green.
- **Full-stack-journey verdict: `no-impact`** — no rendered critical-path / UI surface.
