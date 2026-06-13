# Task 168: Flaky activation.api test — Body already read -- Progress

Type: bug

## Spec
- [x] spec

## Frontend

### Fix: buffer the response body in apiFetch before any await (close the GC body-read race)
Root cause confirmed by deterministic Docker reproduction: GC finalizes the unread undici/MSW
body in the gap between `fetch()` and `response.json()` that Task 8's `apiFetch` opened. A unit
RED cannot pin a GC race in normal runs; the existing tests (`fetch.api.test.ts`,
`activation.api.test.ts`, `login.api.test.ts`) characterize all paths and must stay green, and
the race is reproduced under forced GC (`--expose-gc`) in a CI-matched Linux container.
- [x] reproduce (Docker node:22.13.0 + forced GC: pre-fix 25/25 fail; delay-only 0/95 — GC is the cause)
- [S] red-frontend-api (no deterministic unit RED for a GC race; existing tests guard behaviour)
- [x] green-frontend-api (fetch.api.ts: buffer body via arrayBuffer before the router import; null-body guard)
- [x] verify (local full `vitest run` 23/23; lint/type-check clean; Docker forced-GC full suite 0/25)
