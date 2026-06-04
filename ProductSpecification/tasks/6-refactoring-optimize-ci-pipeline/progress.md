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
- [x] Verify in a PR run (#117, run 26971042826) that the "Starting Testcontainer" path no longer fires; measure surefire delta

  **Result — confirmed, but net gain is small (honest finding).** Listener logged `Local db server found` → recreated `rpm_ddd` on the shared service; **zero** Testcontainers/ryuk activity; all **119** tests green.
  - surefire: **53.3s → 43s (−10.3s)** — the Testcontainers cold-start (ryuk + postgres pull/boot) left the test phase.
  - BUT the postgres pull+boot **moved** into the new "Initialize containers" phase (**+~9.6s**: pull ~3.5s + ~6s wait-for-healthy). On **ephemeral** CI runners the image isn't cached between runs, so the cold-start was **relocated, not eliminated** → net wall-clock ≈ **−1…2s**.
  - Maven wall 118.5s → 93s and build job 132s → 117s, but most of that is run-to-run **dep-resolution variance** (14s vs 23.8s), not this change.
  - Tried tightening the healthcheck (`interval 2s` / `start-period 1s`) — **no effect** (run 26971774419): the ~6s wait is the GitHub runner's own service-health polling backoff (2s→3s→…), not controlled by `--health-*`. Reverted to `interval 5s / retries 10`.
  - **Real remaining levers:** 43s **CPU-bound** surefire (→ matrix shard) + Steps 3–5. The change still pays off as a deterministic test path (no flaky Testcontainers) and sets up sharding (one shared DB per shard).
- [S] (Decision) shard `*IntegrationTest` across a matrix by JUnit tag — **deferred (last resort).** Per the detailed analysis: the 43s surefire is ~half **one-time Spring context boot (~18s)** + ~21s parallel exec bounded by the slowest test (`StaleIncompletePublication` 15.77s, an async wait). Each matrix shard is a separate runner → **duplicates** the ~18s context boot + ~9.6s service-container init + compile/deps, so 2 shards yield only ~43s→~30s (**≈−13s**, not −20) at **~2× runner-minutes** + a brittle tag/class partition + Allure-merge-across-shards + Jacoco/Codecov merge. Poor ROI vs. Steps 4-5. If revisited, attack the **one-time context boot** (the real half), not raw sharding.
- [S] `/refactor` → commit — N/A: change is CI YAML only, no code to refactor.

### Step 3: Fix Maven dependency cache (24s) — NO-OP (premise disproved)
- [x] Diagnose why ~19s of dependency resolution runs despite `cache: maven` (cache key / restore-keys)

  **Finding (run #117 log): there is no dependency-cache problem.** `cache: maven` **hits** (`Cache hit for: setup-java-Linux-x64-maven-…`, ~25 MB, restored in ~0.4s at 18:20:49). The Maven build does **zero** dependency downloads — **0** `Downloading from`/`Downloaded from` lines in the entire log. The ~12-14s `Scanning → first goal` gap is **Maven plugin/reactor model resolution from the local repo** (CPU/IO between "Building rpm-ddd" 18:20:53 and the allure-relocation warning 18:21:05), **not** network resolution — a bigger cache cannot shrink it. The baseline's "~19s of dependency resolution despite cache" was a **misattribution** (Maven core model-build time, not downloads).
  - Adjacent real waste, but it belongs to **Step 5** (frontend), not here: `frontend:install-node-and-npm` **re-downloads Node 22.13.0 + npm 10.9.0 every run** (~3s) — uncached because it runs inside the Maven build; the parallel frontend jobs already cache npm via `setup-node`.
- [S] Correct the cache config; confirm warm-cache resolution is near-instant — **nothing to fix**: cache already hits with zero downloads. Maven-core model-build (~12s) is not cache-addressable and risky to touch (`-o` offline / thread flags give marginal, fragile gains).
- [x] commit

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
