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
- [~] implement (rename build.yml `name:`; update deploy.yml `workflow_run.workflows` in the SAME commit)
- [ ] verify (actionlint; confirm deploy.yml reference matches the new name; main-push dispatch to confirm deploy still triggers)
- [ ] commit

### Step 3: Add timeout-minutes to jobs
- [ ] implement (timeout-minutes on build/frontend/e2e/code-quality/nightly jobs)
- [ ] verify (actionlint)
- [ ] commit

### Step 4: Concurrency on build.yml (cancel superseded PR runs)
- [ ] implement (top-level concurrency group keyed on workflow+ref, cancel-in-progress)
- [ ] verify (actionlint; confirm main/Pages deploy is not cancelled mid-flight — exempt or guard)
- [ ] commit

### Step 5: Least-privilege permissions
- [ ] implement (drop workflow-wide pages/id-token in build.yml; scope to deploy-report job)
- [ ] verify (actionlint; confirm deploy-report still has pages: write + id-token: write)
- [ ] commit

### Step 6: Node version sync
- [ ] implement (align/centralize Node version; reconcile allure-report `lts/*` vs pinned 22.13.0)
- [ ] verify (actionlint)
- [ ] commit

### Step 7: Nightly backend teardown
- [ ] implement (use captured BACKEND_PID in an `if: always()` teardown step)
- [ ] verify (actionlint; workflow_dispatch nightly to confirm clean teardown)
- [ ] commit

## Full-Stack Journey
- [S] fullstack-journey (no-impact: pure CI/infra, no rendered critical-path or UI surface)
