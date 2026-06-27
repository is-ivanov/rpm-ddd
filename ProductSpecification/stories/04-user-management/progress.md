# Story 4: User management — admin user grid & create user — Progress

## Spec
- [x] interview
- [x] story
- [x] mockups
- [x] api-spec
- [x] test-spec

## Backend Scenarios (01_API_Tests.md)

> Implementation order: List users (GET) → Create user validation (POST) → Create user happy path (POST).
> Foundation work threaded through these scenarios: add `timeZone`, `updatedAt`, `updatedBy` to the
> `User` aggregate (+ migration); add the list read-model + `GET /api/admin/users`; add `timeZone` to
> `RegisterUserRequest`/`RegisterUserCommand`/`User.register(...)` and to `GET /api/auth/me`.

### Scenario 1.1: Authenticated user lists all users with resolved actor names
- [x] red-acceptance (UserGridIntegrationTest — @ExpectedToFail; GET /api/admin/users stub → 500; RED confirmed, prediction all-YES)
- [x] design (Option A: read-model view-entity + ORM-resolved actor names + Null-Object "System"; ADR written)
- [S] red-usecase (ListUsersService is a pure pass-through; trivial-test gate — covered by L1 acceptance + L3 db adapter)
- [S] green-usecase (no usecase logic; UserSummary + UserSummaryQuery created in red/green-adapter db, ListUsersService + controller wiring in green-acceptance)
- [S] red-domain (activate only if coverage/design-preview finds testable domain logic)
- [S] green-domain
- [~] adapters-discovery
- [ ] green-acceptance

### Scenario 2.1: Create with a duplicate login returns a field-level 422
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 3.1: Create user with a timezone succeeds and appears in the grid
> Extends the existing registration acceptance test (UserRegistrationIntegrationTest) — add `timeZone`
> to the request and assert the new user is listed in the grid. Do NOT create a parallel acceptance class.
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Integration Scenarios (06_Integration_Tests.md)
(none — create-user reuses the existing event → JWT → activation-email pipeline unchanged; activation
email is asserted as a side effect of backend Scenario 3.1)

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1.1: Sidebar shows an Admin Center group with a Users item
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 1.2: Clicking Users navigates to the Users page inside the shell
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 2.1: Grid renders all columns and rows from the API
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 2.2: Grid shows a loading state while fetching
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 3.1: Typing in a column filter narrows the rows client-side
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 3.2: Clicking a column header sorts the rows
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 3.3: Timestamps show relative time with an absolute-on-hover tooltip
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.1: Register user opens a modal with the timezone pre-filled
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 4.2: Modal shows a loading state during submission
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.1: Successful create closes the modal and refreshes the grid
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.2: Duplicate login or email shows a field-level error
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 6.1: Collapse toggle persists across reload
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

## Full-Stack Journey (07_FullStack_Journey.md)
> Verdict: **extend** — weave the real Story 4 create-user UI into
> frontend/acceptance/tests/fullstack/account-lifecycle.fullstack.spec.ts (replace the direct
> `realAuthBackend.createUserAsAdmin` call with the Admin Center → Users → Register user modal flow).
> Runs once after the frontend scenarios are green, reusing the page Statements built there.
- [ ] fullstack-journey

## Security Scenarios (05_Security_Tests.md)

### Scenario 5.1: SQL injection in create fields is treated as literal text
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.2: Stored XSS in a user name is escaped when rendered in the grid
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.3: Mass assignment — extra fields on create are ignored
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.4: Input length limits on create fields are enforced
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.5: Invalid timezone value is rejected
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 5.6: POST /api/admin/users without a CSRF token returns 403
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Load Scenarios (03_Load_Tests.md)

### Scenario 1.1: List users response time under 200ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 2.1: Concurrent list requests complete under 500ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 3.1: Full list of 500 users returns under 500ms
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Infrastructure Scenarios (04_Infrastructure_Tests.md)

### Scenario 1.1: Database unavailable during list returns 500 with a Problem Detail
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

### Scenario 2.1: Database recovery allows the list after an outage
- [ ] red-acceptance
- [ ] design
- [ ] red-usecase
- [ ] green-usecase
- [S] red-domain
- [S] green-domain
- [ ] adapters-discovery
- [ ] green-acceptance

## Extended (deferred — decide at Story Completion Gate)

> Never executed by /continue. Surfaced here so the Story Completion Gate reviews them before the story closes.

**API (tests/extended/01_API_Tests_Extended.md)**
- [S] E1. Create with a duplicate email returns a field-level 422 (deferred — review at Story Completion Gate)
- [S] E2. Activation updates the audit fields visible in the grid (deferred — review at Story Completion Gate)
- [S] E3. List order is stable when two users share the same createdAt (deferred — review at Story Completion Gate)

**UI (tests/extended/02_UI_Tests_Extended.md)**
- [S] E1. Status multi-select filter lists statuses in lifecycle order (deferred — review at Story Completion Gate)
- [S] E2. Filtering with no matches shows an empty-result state (deferred — review at Story Completion Gate)
- [S] E3. Date-range filter on Created narrows by the underlying instant (deferred — review at Story Completion Gate)
- [S] E4. Cancelling the modal discards input and keeps the grid unchanged (deferred — review at Story Completion Gate)
- [S] E5. Collapsed sidebar restores without a flicker on reload (deferred — review at Story Completion Gate)
- [S] E6. Mobile layout renders the grid and modal (deferred — review at Story Completion Gate)

**Load (tests/extended/03_Load_Tests_Extended.md)**
- [S] E1. Full list of 1000 users returns under 1s (deferred — review at Story Completion Gate)

**Infrastructure (tests/extended/04_Infrastructure_Tests_Extended.md)**
- [S] E1. Database failure during create returns 500 without partial state (deferred — review at Story Completion Gate)

**Security (tests/extended/05_Security_Tests_Extended.md)**
- [S] E1. Oversized timezone string is rejected (deferred — review at Story Completion Gate)
- [S] E2. XSS payload in login and email is escaped in the grid (deferred — review at Story Completion Gate)
- [S] E3. Filter input is never sent to the server (deferred — review at Story Completion Gate)

**Integration (tests/extended/06_Integration_Tests_Extended.md)**
- [S] E1. Create with a timezone still triggers the activation email (deferred — review at Story Completion Gate)
