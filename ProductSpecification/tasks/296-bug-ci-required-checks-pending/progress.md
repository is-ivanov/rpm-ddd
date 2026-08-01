# Task 296: CI required checks stay Pending -- Progress

Type: bug

> CI/infra (YAML) task — standard Java/TS TDD sub-skills do not apply (no unit under test).
> Each step is implement → verify (actionlint; real observation on a PR where noted) → commit.
> Steps 1-3 are ordered: the filter file lands before the workflows that reference it.

## Spec
- [x] spec

## Fix

### Step 1: Single-source path filters
- [x] implement (`.github/path-filters.yml`: anchors `java`/`web`/`tooling`/`ci` → consumer filters `build`, `frontend`, `analysis`)
- [x] verify (YAML parses, anchors expand; every glob of both current `paths:` blocks covered; no dashes in filter names; IDEA inspections clean)
- [x] commit

### Step 2: code-quality.yml — drop trigger `paths:`, gate inside jobs
- [x] implement (all 5 jobs use an in-job `dorny/paths-filter@v4` + step-level `if:`; no `changes` job — see spec §4; added `contents`/`pull-requests: read`)
- [x] verify (actionlint 0 errors across 5 workflows; `pull_request` has no `paths:`, `push: main` still filtered; no job-level `if:` on `Checkstyle`/`PMD`; every `run:` step gated)
- [x] commit

### Step 3: build.yml — drop trigger `paths:`, gate inside jobs
- [ ] implement (remove `paths:` from `pull_request`; `build`/`frontend-build`/`frontend-e2e` use the in-job pattern; `allure-report` gated at job level on upstream job outputs, since it consumes their artifacts)
- [ ] verify (actionlint clean; `deploy-report` and the `main` branch behaviour unchanged)
- [ ] commit

### Step 4: Observe on real PRs
- [ ] verify (doc-only PR → all three required checks report real green; code PR → `Checkstyle`/`PMD`/`build` actually execute and can fail)
- [ ] commit (progress only)

## Full-Stack Journey
- [S] fullstack-journey (no-impact: pure CI/infra, no rendered critical-path or UI surface)