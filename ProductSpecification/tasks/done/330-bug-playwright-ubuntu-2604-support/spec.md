# Task 330: Playwright Ubuntu 26.04 support

Type: bug
Issue: #330

## Problem

GitHub's `ubuntu-latest` runner image is now Ubuntu 26.04. Playwright 1.60 cannot install Chromium
on that OS, so the `frontend-e2e` job in `build.yml` fails at the "Install Playwright browsers" step:

```
Cannot install dependencies for ubuntu26.04-x64 with Playwright 1.60.0!
Error: ERROR: Playwright does not support chromium on ubuntu26.04-x64
```

Effects:

- `frontend-e2e` fails on every PR and on every push to `main`, so the browser E2E suite never runs.
- `allure-report` depends on it and fails as well.
- `nightly-fullstack-e2e.yml` also uses `runs-on: ubuntu-latest`, so the nightly full-stack journey
  fails the same way.
- `deploy.yml` is triggered by the `CI` workflow completing on `main`, so deploys may be blocked.

Seen on PR #328, run 40952983485. The individual Dependabot PRs passed `frontend-e2e` shortly
before this, so the runner image changed between those runs and #328.

## Solution

Upgrade `@playwright/test` in `frontend/package.json` from `^1.60.0` to the latest 1.64.x.
Ubuntu 26.04 support first appears in 1.61.0: `ubuntu26.04` is missing from
`packages/playwright-core/src/server/registry/index.ts` at `v1.60.0` and present from `v1.61.0`
onwards. Upgrade `allure-playwright` as well if it needs a newer Playwright.

Pinning `runs-on: ubuntu-24.04` was considered and rejected: it only delays the problem until that
image is retired.

Verification:

- The local mocked E2E suite (`npm run test:e2e`) passes on the new Playwright version.
- On the PR, `frontend-e2e` and `allure-report` pass.
- `nightly-fullstack-e2e` is run with `workflow_dispatch` against the branch and passes.

## Key Files

- `frontend/package.json`, `frontend/package-lock.json`
- `.github/workflows/build.yml` (`frontend-e2e` job, `npx playwright install --with-deps chromium`)
- `.github/workflows/nightly-fullstack-e2e.yml`

## Reproduction

1. Push any commit that triggers `build.yml` (for example, a PR that touches `frontend/**`).
2. `frontend-e2e` fails at "Install Playwright browsers" after about 17 seconds.
