# Task 296: CI required checks stay Pending

Type: bug
Issue: #296

## Problem

The `main` ruleset (id 10553909) requires three status checks: `build` (from `build.yml`) and
`Checkstyle` + `PMD` (from `code-quality.yml`). Both workflows gate their `pull_request` trigger
with a `paths:` filter.

When a PR touches no file matching a workflow's filter, GitHub never starts that workflow, so its
checks are never reported. A workflow skipped by path filtering leaves its checks in a **Pending**
state and a PR requiring them is blocked from merging — with no timeout and no auto-resolution. The
check is yellow forever and the only escape is a ruleset bypass.

Two failure classes exist today:

1. **Filter drift between the two workflows.** `build.yml` lists `package.json`,
   `package-lock.json`, `allurerc.mjs` and `Dockerfile` in `paths:`; `code-quality.yml` does not. A
   PR touching only the **root** `package-lock.json` gets a green `build` but permanently yellow
   `Checkstyle` + `PMD`.
2. **Doc-only PRs.** A PR touching only `ProductSpecification/**`, `*.md` or `.claude/**` matches
   *neither* workflow, so **all three** required checks stay yellow.

Rulesets cannot express "required only when code changed": ruleset `conditions` support `ref_name`
only, not file paths. The fix therefore has to live in the workflows.

## Solution

Move path filtering out of the workflow **trigger** and into the **jobs**, so every required check
always reports a real conclusion.

1. **Single source of filters.** New `.github/path-filters.yml` holding YAML-anchor building blocks
   (`java`, `web`, `tooling`, `ci`) composed into one filter per consuming job (`build`, `frontend`,
   `analysis`). Both workflows reference it via `dorny/paths-filter`'s `filters:` file form, so the
   drift that caused failure class 1 cannot recur.
2. **Drop `paths:` from the `pull_request` trigger** of `build.yml` and `code-quality.yml`. The
   `push: main` trigger keeps its filter — nothing is gated on `main`.
3. **Required jobs — always run, gate the steps** (`build`, `Checkstyle`, `PMD`). Each job runs a
   `dorny/paths-filter` step itself (no `needs`), and its expensive steps carry
   `if: steps.filter.outputs.<name> == 'true'`. A doc-only PR produces a *genuine* `success` in
   ~20 s instead of a `skipped` conclusion.
4. **Non-required jobs — the same in-job pattern** (`SpotBugs`, `Spotless`, `Frontend Lint`,
   `frontend-build`, `frontend-e2e`). A shared `changes` job feeding job-level `if:` was considered
   and rejected: `needs: changes` puts that job's runner spin-up (~15 s) on the critical path of
   *every* PR — including the common code-PR path — to save a few spin-ups on rare doc-only PRs.
   The exception is `allure-report`, which consumes artifacts produced by other jobs and so must be
   gated at job level; it reads job outputs from `build` / `frontend-build` / `frontend-e2e`
   rather than a `changes` job.
5. **Push runs are never gated.** The `paths-filter` step carries
   `if: github.event_name == 'pull_request'`, and every gate reads
   `github.event_name != 'pull_request' || steps.filter.outputs.<name> == 'true'`. A skipped step
   has empty outputs, so non-PR runs take the first arm and do the full job — no reliance on
   `paths-filter`'s push-event git-diff semantics. The `push: main` triggers keep their `paths:`
   lists, which is what narrows main runs.
6. **Explicit least-privilege permissions.** `dorny/paths-filter` reads the PR's changed-file list
   through the REST API, which needs `pull-requests: read`. `code-quality.yml` had no
   `permissions:` block and inherited the repo default; it now declares
   `contents: read` + `pull-requests: read`, matching the style `build.yml` already uses.

### Why the required jobs do NOT use job-level `if:`

A job skipped by `if:` reports conclusion `skipped`, and branch protection counts `skipped` as
**success**. Using that for a required job makes the gate **fail open**: a typo in a glob, a new
source directory missing from the filter, or a failed `needs` dependency would silently turn
`Checkstyle`/`PMD` green. Keeping the required jobs always-running removes that failure mode — the
cost is one runner spin-up (~20 s) on PRs that change nothing relevant.

The required jobs also deliberately avoid `needs: changes`: a failing `changes` job would skip them,
re-introducing the same fail-open path. They pay a duplicated `paths-filter` step in exchange for
being self-contained, and the duplication is only of the *invocation* — the filter definitions stay
single-sourced in `.github/path-filters.yml`.

### Out of scope

Changing the ruleset's required-check list. The three required contexts stay as they are.

## Key Files

- `.github/path-filters.yml` (new)
- `.github/workflows/build.yml`
- `.github/workflows/code-quality.yml`

## Reproduction

1. Open a PR against `main` that changes only the root `package-lock.json` — e.g. #291
   (`bump dompurify from 3.4.11 to 3.4.12`).
2. `gh pr view 291 --json statusCheckRollup` → only `CI` jobs are present; no `Checkstyle`, no `PMD`.
3. `gh run list --branch dependabot/npm_and_yarn/dompurify-3.4.12` → only `CI` runs; `Run Code
   Quality Tools` never triggered.
4. The GitHub PR page shows `Checkstyle` and `PMD` yellow, labelled "Expected — Waiting for status
   to be reported", and merging is blocked.

Second class: open a PR changing only a file under `ProductSpecification/` — all three required
checks are yellow.