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
**application did not deploy** (merge commit `886bd9e`, both run attempts failed). Locally on
Windows it passes (5/5 full `vitest run`); it reproduces on the Linux CI runner.

## Root cause

Introduced by Task 8 (PR #167) via the new `apiFetch` wrapper
(`frontend/src/app/logic/fetch.api.ts`). Previously `validateActivationToken` did `fetch(...)`
then immediately `await response.json()`. Now an extra async hop sits between them:

```
const response = await fetch(...)
await redirectToLoginWhenUnauthorized(response.status)  // -> await import('@/router')  (cold first load)
return response                                          // caller then: await response.json()
```

`activation.api.test.ts` does not import `@/router` statically, so this is the **first cold
load** of the router module (parses the router plus every route component). That heavyweight
`await import('@/router')` widens the window between the response arriving and its body being
read, exposing an MSW v2 + undici body-stream race that manifests on the Linux CI runner.
(`fetch.api.test.ts` imports `@/router` statically, so the dynamic import is cache-warm there
and the race does not show.)

## Solution

In `apiFetch`, perform the router import / redirect **only when `status === 401`** — the
redirect is only ever needed for 401 (`shouldRedirectToLogin` already requires it). For
200 / 422 / 403 the function returns the response with no extra async hop, restoring the
back-to-back `fetch` → `json()` timing and closing the race window for the body-reading paths.
All existing behaviour stays unchanged; the redirect-on-401 path is preserved.

## Key Files

- `frontend/src/app/logic/fetch.api.ts` — gate the redirect/router import behind `status === 401`
- `frontend/src/app/__tests__/fetch.api.test.ts` — existing guard (401 redirect, 403 stay, 401 via activation)
- `frontend/src/features/auth/__tests__/activation.api.test.ts` — the flaky test (200 success, 422 expired)

## Reproduction

- CI (Linux): run the full `vitest run` (frontend-build job) — intermittently/reliably fails on
  the 200 success-path test with "Body has already been read".
- Local (Windows): not reproducible (5/5 green full runs).
