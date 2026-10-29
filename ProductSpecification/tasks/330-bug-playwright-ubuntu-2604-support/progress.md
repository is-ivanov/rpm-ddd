# Task 330: Playwright Ubuntu 26.04 support -- Progress

Type: bug

> CI/tooling task: the standard Java/TS TDD sub-skills do not apply because there is no unit under test.
> Each step is implement → verify → commit.

## Spec
- [x] spec

## Fix

### Step 1: Upgrade Playwright
- [ ] implement (`@playwright/test` → latest 1.64.x; `allure-playwright` if required; regenerate `frontend/package-lock.json`)
- [ ] verify (`npm run lint`; `npm run test:e2e` locally)
- [ ] commit

### Step 2: Observe on CI
- [ ] verify — PR: `frontend-e2e` and `allure-report` pass
- [ ] verify — `nightly-fullstack-e2e` started with `workflow_dispatch` on the branch passes
- [ ] commit (progress only)

## Full-Stack Journey
- [S] fullstack-journey (no-impact: tooling upgrade, no rendered critical-path change; journey is run in Step 2 only as a check)
