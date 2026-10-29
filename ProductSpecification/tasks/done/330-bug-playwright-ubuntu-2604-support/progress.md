# Task 330: Playwright Ubuntu 26.04 support -- Progress

Type: bug

> CI/tooling task: the standard Java/TS TDD sub-skills do not apply because there is no unit under test.
> Each step is implement → verify → commit.

## Spec
- [x] spec

## Fix

### Step 1: Upgrade Playwright
- [x] implement (`@playwright/test` 1.60.0 → 1.64.1; `allure-playwright` 3.9.0 kept, its peer range is `>=1.53.0`)
- [x] verify (`npm run lint`; `npm run test:e2e` locally)
- [x] commit

### Step 2: Observe on CI
- [x] verify — PR: `frontend-e2e` and `allure-report` pass (#331, run 40956507691)
- [x] verify — `nightly-fullstack-e2e` started with `workflow_dispatch` on the branch passes (run 40956519349)
- [x] commit (progress only)

## Full-Stack Journey
- [S] fullstack-journey (no-impact: tooling upgrade, no rendered critical-path change; journey is run in Step 2 only as a check)
