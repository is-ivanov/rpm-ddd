# Task 168: Flaky activation.api test — Body already read

Type: bug
Issue: #168

## Problem

The frontend unit test `src/features/auth/__tests__/activation.api.test.ts > Activation API
Client > returns the login and email for a valid activation token` intermittently fails on CI:

```
TypeError: Body is unusable: Body has already been read
 ❯ validateActivationToken src/features/auth/logic/activation.api.ts:17:26   // return (await response.json())
```

On the success (200) path the body is read exactly once, yet undici reports it as already
consumed. The failure fails the `frontend-build` job → fails the **Java CI with Maven**
workflow on `main` → the dependent **Docker Build and Push** job is skipped, so the
**application did not deploy** (merge commit `886bd9e`, both run attempts failed).

## Root cause (confirmed)

**Garbage collection finalizes the unread undici/MSW response body during the gap between
`fetch()` resolving and `response.json()` being read.**

Introduced by Task 8 (PR #167) via the new `apiFetch` wrapper
(`frontend/src/app/logic/fetch.api.ts`). Before Task 8, `validateActivationToken` did
`fetch(...)` then **immediately** `await response.json()` — no gap, never flaked. `apiFetch`
inserted an `await redirectToLoginWhenUnauthorized(status)` → `await import('@/router')` between
the response arriving and the body being read. Under the full suite's memory pressure on the
Linux CI runner, a GC cycle fires inside that gap and releases the still-unread body stream;
the subsequent `response.json()` then throws "Body has already been read".

The mechanism was confirmed by deterministic reproduction in a Linux container (Node 22.13.0,
the CI version): with `--expose-gc` and forced GC churn in the gap, the pre-Task-8/pre-fix code
fails **25/25**; a mere time delay of up to 2000 ms with no GC never fails (0/95). It is the GC,
not the delay, that disturbs the body. The same gap exists on every `apiFetch` body-reading
path (e.g. login via `csrf.ts` → `throwLoginError`), so the defect is a whole class, not one
test — the activation 200 path is simply the one with the heaviest gap (a cold `@/router`
import that first-compiles four route SFCs), so it crossed the threshold on real CI first.

## Solution

Buffer the response body into memory **immediately after `fetch()`, before any `await`**, and
return a reconstructed in-memory `Response`. Once the body is read into an `ArrayBuffer`, no
live undici stream remains for GC to finalize, so callers may `await` freely (router import,
CSRF handshake, …) and still read `.json()`/`.text()` safely. A null-body guard handles
101/103/204/205/304. This closes the entire class (activation, login, and any future
`apiFetch` caller), not just the one observed test.

Trade-off: every response body is read into memory (negligible for the small JSON auth
responses; `apiFetch` is not used for streaming), and the reconstructed `Response` drops
`url`/`redirected`/`type` (unused — the test reads `request.url` from the MSW handler, and the
401 redirect uses the router path).

## Key Files

- `frontend/src/app/logic/fetch.api.ts` — buffer the body before the router import; reconstruct Response
- `frontend/src/app/__tests__/fetch.api.test.ts` — existing guard (401 redirect, 403 stay, 401 via activation)
- `frontend/src/features/auth/__tests__/activation.api.test.ts` — the flaky test (200 success, 422 expired)
- `frontend/src/features/auth/__tests__/login.api.test.ts` — same class, latent (login via csrf.ts)

## Reproduction

Deterministic, in a Linux container matching CI (`node:22.13.0`, `--cpuset-cpus=0-3`,
`CI=true`, `NODE_OPTIONS=--expose-gc`), driving GC churn (allocate + `global.gc()`) in the gap
between `fetch` and `response.json()`:

- Pre-fix `apiFetch` (unconditional `await import('@/router')`): activation **25/25 fail**; full
  suite under continuous GC pressure also fails on `login.api` **15/15**.
- Buffering fix: full suite under the same GC pressure **0/25 fail**; normal run 23/23.

Not reproducible on the Windows host or in the container without forced GC (0/120) — real CI
hit it because the full suite's natural memory pressure triggers GC inside the heaviest gap.
