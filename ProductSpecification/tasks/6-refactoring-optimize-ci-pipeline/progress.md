# Task 6: Optimize CI Pipeline Duration -- Progress

Type: refactoring

## Spec
- [x] spec

## Baseline (measured, PR #116 build job — from Actions log timestamps)
Maven wall = 118.5s. Critical path = `build` (132s); total PR feedback ≈ 2m37s; all other jobs done by ~50s.

| Phase | Time | Share |
|-------|------|-------|
| surefire:test | 53.3s | 45% |
| startup + dependency resolution | 23.8s | 20% |
| frontend (node+ci+build+vitest) | 13.3s | 11% |
| compiler:compile | 9.4s | 8% |
| spotless:check | 9.4s | 8% |
| testCompile | 3.9s | 3% |
| jacoco/jar/repackage/misc | ~4.5s | 4% |

Insight: tests (45%) + dep resolution (20%) dominate; frontend is only 11%. Surefire cold-starts Testcontainers Postgres every run (no shared CI DB on port 54034).

## Decision (Step 1)
- [x] Measure first (done — table above). Re-prioritized by impact: tests > dep cache > spotless > frontend.
- [x] Confirm test approach before editing: **Postgres service container only** (shared-first reuse). Rationale: CI runners are ephemeral and per-run isolated (each PR run gets its own VM + its own service Postgres on `localhost:54034` — no cross-PR sharing or conflict), so the win is purely removing the per-run Testcontainers cold-start (image pull + ryuk + boot), not cross-run reuse. Matrix sharding deferred: it adds runner cost + Allure-merge complexity and is only justified if tests stay CPU-bound after the cold-start is gone — decide from a measured PR run (Step 2 verify). **Perf-tuning flags omitted**: GH Actions `services:` cannot override the container command, so the `fsync=off`/`synchronous_commit=off` tuning from `docker/infra-tests.yml` is not applied; revisit via a manual `docker run` step only if surefire remains CPU-bound.

## Fix

### Step 1: Baseline breakdown — DONE
- [x] Pulled `build` job log via `gh api .../jobs/<id>/logs`, parsed `[INFO] --- goal (exec) ---` deltas → breakdown table above

### Step 2: Speed up backend tests (53s — biggest lever)
- [x] Add a Postgres **service container** in `build.yml` on port 54034 with creds matching `docker/.env`, so `DbContainerTestExecutionListener` reuses it instead of cold-starting Testcontainers
- [~] Verify in a PR run that the "Starting Testcontainer" path no longer fires; measure surefire delta
- [ ] (Conditional) if still CPU-bound: shard `*IntegrationTest` across a matrix by JUnit tag
- [ ] `/refactor` → commit

### Step 3: Fix Maven dependency cache (24s)
- [ ] Diagnose why ~19s of dependency resolution runs despite `cache: maven` (cache key / restore-keys)
- [ ] Correct the cache config; confirm warm-cache resolution is near-instant in a PR run
- [ ] commit

### Step 4: Move spotless off the critical path (9s)
- [ ] Move `spotless:check` out of the CI `build` into the parallel Code Quality workflow (keep local `verify` binding intact)
- [ ] commit

### Step 5: Move frontend off the backend critical path (13s)
- [ ] Run backend `build` without `-P frontend` in CI
- [ ] Add a `frontend-build` job (npm ci + `npm build` + vitest), parallel to `build`, emitting Allure results; wire into `allure-report` `needs`
- [ ] Verify `./mvnw verify -P frontend` still builds everything locally (unchanged)
- [ ] `/refactor` → commit

### Step 6: Re-measure & confirm
- [ ] Trigger a PR → main run, rebuild the breakdown table, record new critical-path time
- [ ] Confirm all 119 backend tests still run and gate the PR; report before/after

## Verification
- CI run timings via `gh run view <id> --json jobs` compared against the baseline table in `spec.md`.
- build.yml triggers only on PR/push to `main` — confirmation requires a PR run, not a task-branch push.
