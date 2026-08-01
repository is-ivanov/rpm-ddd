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
- [ ] implement (remove `paths:` from `pull_request`; `Checkstyle`/`PMD` run always with step-level `if:` on an in-job `dorny/paths-filter`; `SpotBugs`/`Spotless`/`Frontend Lint` gated by a `changes` job)
- [ ] verify (actionlint clean; `push: main` filter untouched; no `if:` on the `Checkstyle`/`PMD` job level)
- [ ] commit

### Step 3: build.yml — drop trigger `paths:`, gate inside jobs
- [ ] implement (remove `paths:` from `pull_request`; `build` runs always with step-level `if:`; `frontend-build`/`frontend-e2e`/`allure-report` gated by a `changes` job, incl. the no-artifacts guard on `allure-report`)
- [ ] verify (actionlint clean; `deploy-report` and the `main` branch behaviour unchanged)
- [ ] commit

### Step 4: Observe on real PRs
- [ ] verify (doc-only PR → all three required checks report real green; code PR → `Checkstyle`/`PMD`/`build` actually execute and can fail)
- [ ] commit (progress only)

## Full-Stack Journey
- [S] fullstack-journey (no-impact: pure CI/infra, no rendered critical-path or UI surface)