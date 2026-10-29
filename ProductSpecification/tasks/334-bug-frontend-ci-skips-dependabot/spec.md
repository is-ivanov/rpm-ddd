# Task 334: Frontend CI skips Dependabot

Type: bug
Issue: #334

## Problem

In `.github/workflows/build.yml`, both `frontend-build` (vitest, SPA build) and `frontend-e2e`
(mocked Playwright) have this job-level condition:

```yaml
if: github.actor != 'dependabot[bot]'
```

So on any PR opened or rebased by Dependabot, both jobs are skipped. The PR still shows green CI,
even though the frontend was never built or tested. This affects exactly the PRs that change frontend
dependencies: `vite`, `vitest`, `zod`, `vue`, and others. Examples: #329 (zod), and every npm PR
batched in #328.

The condition was added in Task 6 (#117), when the frontend moved out of the Maven `-Pfrontend`
build into its own jobs. It appears to have been copied from the Codecov upload (#74) and the Allure
report/Pages publishing (#91), which do need secrets or Pages permissions. The two frontend jobs need
neither: they only run checkout, setup-node, `npm ci`, build, test, Playwright install, `test:e2e`
and upload-artifact.

Before #117, the frontend was built and tested inside the Maven run, which also ran on Dependabot
PRs. So this is a regression.

## Solution

Remove the actor condition from `frontend-build` and `frontend-e2e`. Keep it on `allure-report`, on
the Codecov step and on report publishing, because those need secrets or Pages permissions.

`allure-report` needs `frontend-build` and `frontend-e2e`, and its own `if:` excludes
`dependabot[bot]`, so it should keep skipping cleanly for Dependabot. Verify this, then confirm on a
real Dependabot PR.

## Key Files

- `.github/workflows/build.yml` (`frontend-build`, `frontend-e2e`, `allure-report`)

## Reproduction

1. Open any Dependabot PR that touches `frontend/` (for example #329).
2. Its checks list shows `frontend-build` and `frontend-e2e` as `skipping`, yet the PR is `CLEAN`
   and green.
