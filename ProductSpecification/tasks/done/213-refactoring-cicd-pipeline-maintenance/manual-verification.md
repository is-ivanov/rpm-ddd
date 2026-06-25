# Task 213 — Manual verification (user-side, post-merge)

These steps can't be verified locally: each needs the workflow pushed to GitHub
and a real run. actionlint is green on all changed workflows; what remains is
confirming runtime behaviour. Run them after the PR lands on `main` (or from the
branch where the workflow exists).

## Step 1 — Nightly failure → auto GitHub issue
- [ ] Temporarily force the nightly to fail (e.g. add `&& exit 1` to the "Run
      full-stack E2E journey" step, or point the seed at a bad host) on a scratch
      branch, then trigger it: **Actions → Nightly Full-Stack E2E → Run workflow**
      (`workflow_dispatch`).
- [ ] Confirm a `nightly-failure`-labelled issue is opened, titled "Nightly
      full-stack E2E failed", linking the failed run URL.
- [ ] Run it failing a **second** time → confirm it **comments on the same issue**
      (dedupe), does NOT open a duplicate.
- [ ] Close the issue, fail it a **third** time → confirm the **closed issue is
      reopened** (not a new one).
- [ ] Revert the forced failure and **close the test `nightly-failure` issue**.

## Step 2 — Rename "Java CI with Maven" → "CI" (deploy coupling)
- [ ] Push a commit to `main` (touching a `build.yml` path filter, e.g. `src/**`).
- [ ] Confirm the `CI` workflow runs to success on `main`.
- [ ] Confirm **`Docker Build and Push` (deploy.yml) still triggers** afterwards
      via `workflow_run` — i.e. the rename didn't silently break the deploy chain
      (image pushed to ghcr.io + Render deploy fired).

## Step 7 — Nightly backend teardown
- [ ] Trigger the nightly (`workflow_dispatch`) and confirm the **"Stop backend"**
      step runs (it has `if: always()`) and reports no error, on both a passing
      and a failing run.

---
Once all three are confirmed, this file has served its purpose — it can stay as
an audit record or be deleted.
