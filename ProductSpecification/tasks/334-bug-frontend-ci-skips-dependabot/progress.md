# Task 334: Frontend CI skips Dependabot -- Progress

Type: bug

> CI (YAML) task: the standard Java/TS TDD sub-skills do not apply because there is no unit under test.
> Each step is implement → verify → commit.

## Spec
- [x] spec

## Fix

### Step 1: Run frontend jobs for Dependabot
- [ ] implement (drop `github.actor != 'dependabot[bot]'` from `frontend-build` and `frontend-e2e`; keep it on `allure-report`, Codecov and publishing)
- [ ] verify (actionlint clean; on our own PR the frontend jobs still run and `allure-report` still passes)
- [ ] commit

### Step 2: Observe on a Dependabot PR
- [ ] verify — a Dependabot PR touching `frontend/` runs `frontend-build` and `frontend-e2e`; `allure-report` skips without failing
- [ ] commit (progress only)

## Full-Stack Journey
- [S] fullstack-journey (no-impact: CI-only change, no rendered critical-path or UI surface)
