# Task 213: CI/CD pipeline maintenance -- Progress

Type: refactoring

> CI/infra (YAML) task — standard Java/TS TDD sub-skills do not apply (no unit under test).
> Each step is implement → verify (actionlint, and `workflow_dispatch` where behaviour must
> be observed) → commit. Steps are independent; ship most-valuable-first.

## Spec
- [x] spec

## Fix

### Step 1: Nightly failure → auto GitHub issue
- [x] implement (`if: failure()` step in nightly-fullstack-e2e.yml: open/reopen/dedupe `nightly-failure` issue, link run, GITHUB_TOKEN)
- [x] verify (actionlint clean; workflow_dispatch forced-failure check is user-side — requires push + real nightly run)
- [x] commit

### Step 2: Safe rename of "Java CI with Maven" (+ deploy.yml coupling)
- [x] implement (renamed build.yml `name:` → `CI`; updated deploy.yml `workflow_run.workflows` + docs/ci-pipeline diagram in the SAME commit)
- [x] verify (actionlint clean; deploy.yml ref == "CI"; no stale "Java CI with Maven" left in repo except historical task docs; main-push dispatch confirmation is user-side)
- [x] commit

### Step 3: Add timeout-minutes to jobs
- [x] implement (timeout-minutes on all 11 jobs in build.yml, code-quality.yml, nightly; deploy.yml out of Step-3 scope per spec Key Files)
- [x] verify (actionlint clean, exit 0)
- [x] commit

### Step 4: Concurrency on build.yml (cancel superseded PR runs)
- [x] implement (top-level concurrency group=workflow+ref; cancel-in-progress guarded to non-main refs so Pages deploy on main is never cancelled)
- [x] verify (actionlint clean; main exempt via `cancel-in-progress: ${{ github.ref != 'refs/heads/main' }}`)
- [x] commit

### Step 5: Least-privilege permissions
- [x] implement (workflow-wide perms reduced to `contents: read`; pages/id-token scoped to deploy-report job only)
- [x] verify (actionlint clean; deploy-report retains pages: write + id-token: write — confirmed via grep)
- [x] commit

### Step 6: Node version sync
- [x] implement (added root `.nvmrc`=22.13.0; all 5 setup-node steps now use `node-version-file: .nvmrc`; allure-report `lts/*` drift removed)
- [x] verify (actionlint clean; grep confirms 0 hardcoded node-version, all 5 use the file; node-version-file resolves from repo root per setup-node docs)
- [x] commit

### Step 7: Nightly backend teardown
- [~] implement (use captured BACKEND_PID in an `if: always()` teardown step)
- [ ] verify (actionlint; workflow_dispatch nightly to confirm clean teardown)
- [ ] commit

## Full-Stack Journey
- [S] fullstack-journey (no-impact: pure CI/infra, no rendered critical-path or UI surface)
