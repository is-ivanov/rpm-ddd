# Task 6: Optimize CI Pipeline Duration

Type: refactoring

## Problem

End-to-end PR feedback time is **~2m37s**, and it is almost entirely gated by a single job. Measured from PR #116 (`maintenance` branch, `pull_request` event):

| Workflow | Job | Duration | On critical path? |
|----------|-----|----------|-------------------|
| Java CI with Maven | **build** (`mvn verify -P frontend`) | **132s** (step "Build with Maven" = 123s) | **YES** |
| Java CI with Maven | frontend-e2e | 50s (Playwright browser install = 32s) | no (∥ build) |
| Java CI with Maven | allure-report | 12s (needs build + e2e) | tail of critical path |
| Java CI with Maven | deploy-report | skipped on PR | — |
| Run Code Quality Tools | checkstyle | 21s | no (∥) |
| Run Code Quality Tools | pmd | 37s | no (∥) |
| Run Code Quality Tools | frontend-lint | 12s | no (∥) |

Everything except `build` finishes by ~50s. The two workflows already run concurrently, finder jobs within each are already parallel, and backend tests already run parallel inside the JVM (`junit.jupiter.execution.parallel.enabled = true`). So the only lever for PR time is the `build` job.

What `mvn verify -P frontend` does serially inside that one 123s step (from `pom.xml`):
- `spotless:check` (bound to `compile`)
- `-P frontend`: node install → `npm ci` → `npm build` → vitest (`generate-resources` / `test` phases)
- backend compile + all `*Test`/`*IntegrationTest` (surefire, full Spring context tests are the slow tier)
- jacoco report + check (`verify`)

Confirmed **not** duplicated in the build: checkstyle and pmd have no phase-bound `<executions>`, so they run only in the dedicated Code Quality workflow — no double work there.

Wasteful overlap observed: the frontend is effectively built/tested up to **three times** per PR — `npm build`+vitest inside the backend Maven run, again in `frontend-e2e` (npm ci + Playwright), and lint in `frontend-lint` (npm ci + eslint).

## Measured breakdown of the build (PR #116, from Actions log timestamps)

Maven wall (`Scanning for projects` → `BUILD SUCCESS`) = **118.5s**. Per execution:

| Phase / execution | Time | Share |
|-------------------|------|-------|
| **surefire:test** (backend tests) | **53.3s** | **45%** |
| startup + dependency resolution | 23.8s | 20% |
| frontend total (install-node 4.4 + npm-ci 4.2 + npm-build 3.3 + vitest 1.4) | 13.3s | 11% |
| compiler:compile | 9.4s | 8% |
| spotless:check | 9.4s | 8% |
| compiler:testCompile | 3.9s | 3% |
| jacoco + jar + repackage + byte-buddy + resources | ~4.5s | 4% |

Key insight from the data: the critical path is dominated by **backend tests (45%)** and **dependency resolution (20%)** — NOT the frontend (only 11%). Inside surefire, the log shows Testcontainers cold-starting Postgres every run (`Local Db server not found ... Starting Testcontainer`, pulling `ryuk` + `postgres:18.3-alpine`) because CI has no shared local DB on the expected port.

## Solution

Attack the `build` job's critical path, ordered by measured impact:

1. **Backend tests (53s) — biggest lever.**
   - Run a **Postgres service container** on the port the `DbContainerTestExecutionListener` probes (`54034`), with matching creds, so tests use the shared-first path and skip Testcontainers cold-start (ryuk + postgres pull/boot). See `project_reusable-test-infra-pattern`.
   - Tune test parallelism (surefire `forkCount` / JUnit parallel is already enabled) and/or shard `*IntegrationTest` across matrix jobs by JUnit tag if CPU-bound on one runner.
2. **Dependency resolution (24s).** 19s elapse between `Building` and the first goal despite `cache: maven` — the cache key is likely missing. Fix the Maven `~/.m2` cache so resolution is near-instant on warm cache.
3. **spotless:check (9s).** It is a format gate, not part of backend test correctness — move it to the (already parallel) Code Quality workflow so it leaves the critical path.
4. **Frontend (13s).** Run backend `verify` **without** `-P frontend` in CI and build+test the frontend (`npm build` + vitest) in its own job parallel to `build` — also ends the triple-build of the frontend.
5. **Re-measure** — fresh PR run, rebuild the breakdown, record the new critical-path time.

Rough projection: steps 3+4 ≈ −22s, step 2 ≈ −15s; the main prize is step 1 (53s). Critical path realistically goes from 132s toward ~70-80s, lower with test sharding.

## Key Files

- `.github/workflows/build.yml` — `build` job (drop `-P frontend` in CI), add a parallel `frontend-build` job, caches
- `.github/workflows/code-quality.yml` — possible consolidation of duplicate `npm ci`
- `pom.xml` — `frontend` profile bindings; keep local `./mvnw verify -P frontend` working unchanged
- `frontend/package.json` — npm scripts reused by the split job

## Notes

- **Measure before cutting.** "Build with Maven" is one opaque 123s block in Actions logs; the frontend vs backend split is unknown until step 1.
- Keep the **local** developer flow intact — `./mvnw verify -P frontend` must still build everything in one command. Only the CI invocation changes.
- build.yml does not trigger on task-branch pushes; final green/timing confirmation needs a PR → `main` run (same constraint noted in Task 3).
- Do not regress correctness for speed: the full backend test suite (119 tests) must still run and gate the PR.
- Static analysis (`checkstyle`/`pmd`/`frontend-lint`) is off the critical path — leave it; no change needed.
