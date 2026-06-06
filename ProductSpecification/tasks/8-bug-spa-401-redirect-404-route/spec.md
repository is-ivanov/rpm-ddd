# Task 8: SPA global 401→login redirect and 404 catch-all route

Type: bug

## Problem

Two related front-of-app UX gaps were found while analyzing Story 1's security posture
(`SecurityConfig` is a correct deny-by-default allow-list — that part stays as is). The gaps
are in how unauthenticated and unknown-route requests surface to the user:

1. **No global 401 handler on the frontend.** Each API client parses the error body and throws a
   local error (see `frontend/src/features/auth/logic/login.api.ts`). There is no fetch wrapper,
   axios interceptor, or router guard that reacts to a `401` globally. So when a session expires or
   an unauthenticated user triggers a protected `/api/**` call, the backend returns a `401` Problem
   Detail JSON and the user is left on the current view with only a thrown JS error — they are never
   sent to the login page.

2. **No SPA catch-all → unknown routes show raw backend JSON, not a 404 page.**
   - Backend: `SpaForwardingController` forwards only the three explicit paths `/`, `/login`,
     `/activate` to `index.html`. It is not a wildcard. The security chain runs before routing, so a
     browser deep-link / refresh on any other path (e.g. `GET /dashboard`) falls through to
     `anyRequest().denyAll()` and returns a raw `401`/`403` JSON body in the browser — never the SPA
     shell, never a 404 page.
   - Frontend: `frontend/src/router/index.ts` defines only `home`, `login`, `activate` with **no**
     catch-all `:pathMatch(.*)*` route, so even in-app navigation to an unknown route renders nothing
     meaningful.

   A genuine in-app 404 page therefore requires BOTH a backend catch-all forward (so refresh /
   deep-link returns `index.html`) AND a Vue router catch-all route (so the SPA renders a NotFound
   view).

## Solution

Preserve the deny-by-default posture for `/api/**` throughout — neither change may make any
`/api/**` path reachable without authentication.

1. **Global 401 → login redirect.** Introduce a single place where every API response is checked.
   On a `401` (and only when not already on the login route, to avoid loops), navigate the SPA to
   `/login`. The redirect/branching decision is pure logic and must be unit-tested; the API clients
   delegate to this shared layer instead of each re-implementing error handling. A `403` (e.g. CSRF
   failure) is NOT a session-expiry signal and must NOT redirect.

2. **SPA catch-all 404.**
   - Backend: extend the SPA forward so unknown **non-`/api`, non-asset** GET routes forward to
     `index.html` (HTTP 200) while `/api/**` and unmatched verbs keep the existing deny-by-default
     behavior. The allow-list in `SecurityConfig` must still gate what is publicly reachable — the
     forward must not widen the security surface.
   - Frontend: add a trailing catch-all route (`/:pathMatch(.*)*`) mapping to a new `NotFoundPage`
     view that renders a friendly 404 with a link back to a known route (navigation via UI, not URL).

## Key Files

- `frontend/src/features/auth/logic/login.api.ts` — existing per-call error handling (pattern to share)
- `frontend/src/features/auth/logic/activation.api.ts` — second API client to route through the shared layer
- `frontend/src/router/index.ts` — add the catch-all 404 route
- `frontend/src/features/**/components/` — new `NotFoundPage.vue` (location per feature-structure rules)
- `src/main/java/by/iivanov/rpm/shared/infrastructure/web/SpaForwardingController.java` — catch-all forward
- `src/main/java/by/iivanov/rpm/iam/auth/infrastructure/SecurityConfig.java` — verify allow-list still gates `/api/**` (likely unchanged)

## Reproduction

1. **401 redirect gap:** With no session, trigger any protected `/api/**` call from the SPA (or let a
   session expire, then act). Observe: backend returns `401` JSON, the JS call throws, and the user
   stays put — no navigation to `/login`.
2. **404 gap (deep-link):** In the browser, navigate directly to `GET /dashboard` (any path other than
   `/`, `/login`, `/activate`). Observe: a raw `401`/`403` JSON body renders in the browser instead of
   a 404 page or the SPA shell.
3. **404 gap (in-app):** From inside the running SPA, navigate to an unknown client route. Observe: the
   router matches nothing and no NotFound view is shown.