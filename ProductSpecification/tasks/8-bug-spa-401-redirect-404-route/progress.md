# Task 8: SPA global 401→login redirect and 404 catch-all route — Progress

Type: bug

## Spec
- [x] spec

## Backend

### Fix: SPA catch-all forward (unknown non-/api GET → index.html), deny-by-default preserved
- [x] red-acceptance — integration test: deep-link `GET /dashboard` → 200 `text/html` (index.html); `GET /api/<unknown>` (unauth) → still 401; `GET /assets/**` unaffected
- [~] adapters-discovery — web adapter only (`SpaForwardingController`); no usecase/domain; expect `[S]` simple delegation
- [ ] green-acceptance — `SpaForwardingController` forwards catch-all non-`/api`, non-asset GET routes to `index.html`; confirm `SecurityConfig` allow-list still gates `/api/**`

## Frontend

### Fix A: Global 401 → login redirect (shared API error layer)
- [ ] red-playwright — E2E: a protected action while unauthenticated (or after session expiry) lands the user on `/login`
- [ ] red-frontend — logic test: shared response handler redirects to `/login` on 401; does NOT redirect on 403; does NOT redirect when already on `/login`
- [ ] green-frontend — implement shared 401-handling logic
- [ ] red-frontend-api — API client test: a 401 from a protected `/api/**` call routes through the shared layer and triggers the redirect path; existing clients (`login.api`, `activation.api`) delegate to it
- [ ] green-frontend-api — route existing API clients through the shared layer
- [ ] align-design — `[S]` (redirect reuses the existing `/login` page; no new visible component)
- [ ] green-playwright
- [ ] demo

### Fix B: SPA 404 catch-all route + NotFoundPage
- [ ] red-playwright — E2E: navigating to an unknown client route renders the NotFound view with a link back to a known route
- [ ] red-frontend — `[S]` (catch-all route + NotFound view are presentational/config; no branching/transform logic)
- [ ] green-frontend — `[S]` (paired with the skipped logic test)
- [ ] align-design — add catch-all route `/:pathMatch(.*)*` in `router/index.ts`; build `NotFoundPage.vue` per mockup/feature-structure rules (`data-testid`s for E2E)
- [ ] green-playwright
- [ ] demo