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

### Step 4: Move spotless off the critical path (9s) — DONE
- [x] Move `spotless:check` out of the CI `build` into the parallel Code Quality workflow (keep local `verify` binding intact)

  CI `build` now runs `mvn verify -B -Pfrontend -Dspotless.check.skip=true` → spotless leaves the critical path (~−9s). Added a parallel `spotless` job to `code-quality.yml` (mirrors `checkstyle`/`pmd`: checkout → setup-java → `mvn spotless:check -B`). **pom.xml untouched** — the `compile`-phase binding stays, so local `./mvnw verify` still runs spotless. Verified locally: with the flag → `Spotless check skipped` / BUILD SUCCESS; standalone `mvn spotless:check -B` → checks 162 files / BUILD SUCCESS.
- [x] commit

### Step 5: Move frontend off the backend critical path (13s) — DONE
- [x] Run backend `build` without `-P frontend` in CI

  **Constraint discovered (spec didn't cover it):** the `main` `app-jar` is the deploy artifact — `deploy.yml` downloads it, wraps it via `Dockerfile.deploy`, pushes to ghcr, triggers Render. The SPA reaches the jar only via `copy-frontend-dist` under `-Pfrontend`. Dropping it wholesale on main → frontend-less production jar. **Backend tests are safe without the frontend**: `SpaServingIntegrationTest` forwards `/`→`/index.html` served from a committed **test fixture** `src/test/resources/static/index.html`, not the real build.
  - Resolution (Option D, user-approved): PR build runs `mvn verify -B -Dspotless.check.skip=true` (backend only — also drops the ~3s Node re-download from Step 3). **main** build runs `-Pfrontend -Dnpm.test.skip=true` → bakes SPA into the deploy jar but skips vitest (runs in `frontend-build`). Two `if:`-gated steps on `github.ref`.
- [x] Add a `frontend-build` job (npm ci + `npm build` + vitest), parallel to `build`, emitting Allure results; wire into `allure-report` `needs`

  New `frontend-build` job (mirrors `frontend-e2e`): setup-node(cache npm) → npm ci → npm run build → npm run test (vitest) → uploads `allure-results-frontend`. Added to `allure-report` `needs: [build, frontend-build, frontend-e2e]` + a download step. vitest now lives in **one** place; result files use unique UUIDs so the 3-artifact merge into `target/allure-results` doesn't collide.
- [x] Verify `./mvnw verify -P frontend` still builds everything locally (unchanged)

  pom: `npm-test` execution gained `<skip>${npm.test.skip}</skip>` (new property, default **false**). Verified locally via `frontend:npm@npm-test`: default → `Running 'npm run test'` (vitest, 1 passed); with `-Dnpm.test.skip=true` → `Skipping execution`. Both BUILD SUCCESS. Local `./mvnw verify -P frontend` keeps running vitest (default false).
- [S] `/refactor` → commit — N/A: changes are pom/YAML config, no code to refactor. IDE inspection on pom.xml attempted but the IDEA MCP errored (`getDescription must not be null`); pom validity proven by the two successful Maven runs.

### Step 6: Re-measure & confirm — DONE
- [x] Trigger a PR → main run, rebuild the breakdown table, record new critical-path time
- [x] Confirm all 119 backend tests still run and gate the PR; report before/after

  **Measured on PR run 26974213883 (commit `37f2fc8`, PR #117).** All jobs green; `build` is the sole critical path (every parallel job finishes inside its window).

  **Before / After (critical path = PR feedback time):**

  | Metric | Baseline (PR #116) | After (run 26974213883) | Δ |
  |--------|---:|---:|---:|
  | **PR feedback** (run start → `allure-report` end) | **2m37s (157s)** | **1m57s (117s)** | **−40s (−25%)** |
  | `build` job | 132s | 95s | −37s |
  | Maven wall (`Scanning`→`BUILD SUCCESS`) | 118.5s | 68s | −50s |
  | surefire:test | 53.3s | ~44s | −9s |

  **Parallel jobs (all within the 95s `build` window, off the critical path):** `frontend-build` 18s · `frontend-e2e` 47s · `allure-report` 14s · Code-Quality `Spotless` 43s / `PMD` 42s / `Checkstyle` 22s / `Frontend Lint` 18s.

  **Correctness confirmed in the build log:** `Spotless check skipped`; **0** frontend goals in `build`; PR step `mvn verify -B -Dspotless.check.skip=true` (no `-Pfrontend`); `Local db server found` (service container, no Testcontainers); **Tests run: 119, Failures: 0**.

  **Where the time went (vs. spec projection):** the prizes were Step 4 (spotless −9s off path) + Step 5 (frontend −13s + ~3s node download off path), which together cut the `build` critical path; Step 2 (service container) is net ~neutral on wall-clock but removes Testcontainers flakiness; Step 3 (dep cache) was a no-op (already optimal). Sharding was deferred (poor ROI).

  **⚠️ Open verification (post-merge):** the `main`-only path (`-Pfrontend -Dnpm.test.skip=true` baking the SPA into the deploy `app-jar`) cannot run on a PR. Confirm on the first `build` after merge to `main` that `app-jar` still contains `static/index.html` and `deploy.yml` ships a complete frontend.

### Step 7 (follow-up): cache the Spotless formatter — DONE
- [x] Give the `spotless` job a dedicated Maven cache so it stops re-downloading the formatter every run

  **Diagnosis (local `-X` + cold-cache repro):** the Spotless job log showed `Index file does not exist…` (harmless incremental-cache miss, ~3s full check) plus **~24s of silent time** before it. Root cause: spotless resolves `com.palantir.javaformat:palantir-java-format:2.90.0` + `com.google.googlejavaformat:google-java-format:1.28.0` (+ transitive) **at goal-execution time**; these aren't in the project dependency graph, so the shared setup-java `cache: maven` (populated by build/checkstyle/pmd, none of which use the formatter) never contains them → re-downloaded from Maven Central every run (invisible under `-B`). Reproduced locally: removing the artifacts from `~/.m2` re-triggers the download; cold `-X` log shows `Resolving artifact … from [central]` + `Writing tracking file …`.
  - **Fix:** `code-quality.yml` `spotless` job now uses a job-owned `actions/cache` on `~/.m2/repository` keyed `${{ runner.os }}-maven-spotless-${{ hashFiles('pom.xml') }}` (dropped `cache: maven` from its setup-java). First run populates it incl. the formatter; subsequent runs restore → the ~24s download disappears.
  - **Honest scope:** Spotless runs **in parallel**, off the critical path (43s job vs 95s `build`), so this saves **CI runner-minutes only — zero effect on PR-feedback wall-clock**. Pure cost optimization.
  - **Confirmation needs 2 runs:** run N populates the new cache key, run N+1 restores it (cache hit) — verify the silent 24s gap is gone on the second post-fix Spotless run.

### Step 8 (follow-up): silence the Allure relocation warning — DONE
- [x] Rename the `allure-junit5` dependency to `allure-jupiter`

  Build logged `[WARNING] io.qameta.allure:allure-junit5:2.35.2 has been relocated to io.qameta.allure:allure-jupiter:2.35.2 (renamed)`. This is a pure artifact **rename at the same version** (BOM-managed 2.35.2) — not a version bump. Changed the `pom.xml` dependency `artifactId` `allure-junit5` → `allure-jupiter`; no code references it (the extension auto-registers via the JUnit Platform ServiceLoader). Verified: `dependency:tree` resolves `allure-jupiter:2.35.2` with the same integration subtree (`allure-junit-platform` → `allure-java-commons` → …) and **no relocation warning**; a sample test run still emits result files to `target/allure-results`.

## Verification
- CI run timings via `gh run view <id> --json jobs` compared against the baseline table in `spec.md`.
- build.yml triggers only on PR/push to `main` — confirmation requires a PR run, not a task-branch push.
