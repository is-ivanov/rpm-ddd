# Task 168: Flaky activation.api test — Body already read -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: gate the router import in apiFetch behind status 401 to close the body-read race
Timing/flaky fix — the defect is a non-deterministic MSW + undici body-stream race widened by a
cold `await import('@/router')` on the success path, not deterministic runtime logic. No
meaningful unit RED can pin a timing race; the existing tests
(`fetch.api.test.ts`, `activation.api.test.ts`) characterize all paths and must stay green.
- [S] red-frontend-api (no deterministic unit RED for a timing race; existing tests guard behaviour)
- [ ] green-frontend-api (fetch.api.ts: import/redirect only when status === 401; all existing tests green)
- [ ] verify (full `vitest run` green; stress loop to confirm no flake)
