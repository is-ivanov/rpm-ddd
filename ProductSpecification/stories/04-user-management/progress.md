# Story 4: User management — admin user grid & create user — Progress

> Terse entries (status + test-class/ADR ref + `see summaries/X` link). The "why" lives in
> `summaries/` + `carryover.md`; see `.claude/rules/workflow.md` → "Updating Progress".

## Spec
- [x] interview
- [x] story
- [x] mockups
- [x] api-spec
- [x] test-spec

## Backend Scenarios (01_API_Tests.md)

> Implementation order: List users (GET) → Create user validation (POST) → Create user happy path (POST).

### Scenario 1.1: Authenticated user lists all users with resolved actor names
- [x] red-acceptance (UserGridIntegrationTest; see summaries/1.1-list-users.md)
- [x] design (ADR read-model view-entity; see summaries/1.1-list-users.md)
- [S] red-usecase (ListUsersService pure pass-through — covered by L1 + L3 db adapter)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (db: UserSummaryQuery view query → red/green-adapter db; rest/usecase [S])
- [x] red-adapter db (JpaUserSummaryQueryTest; see summaries/1.1-list-users.md)
- [x] green-adapter db (UserSummaryView; later deleted L1-dup; see summaries/1.1-list-users.md)
- [x] green-acceptance (see summaries/1.1-list-users.md)

### Scenario 2.1: Create with a duplicate login returns a field-level 422 (web-slice, Level 2)
> Level: L2 web-slice — duplicate login → 422 field error; domain rule already covered at application level. see summaries/2.1-duplicate-login-422.md
- [x] red-adapter rest (UserResourceTest @WebTest — 422 + fieldErrors[login])
- [x] design (Option A validation-failed field error — no ADR)
- [x] green-adapter rest (LoginAlreadyExistsExceptionHandler → 422; see summaries/2.1-duplicate-login-422.md)
- [S] red-usecase (duplicate-login already covered at application level — UserRegistrationPolicy)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance (no L1 test for an error category)

### Scenario 3.1: Create user with a timezone succeeds and appears in the grid (L1 acceptance)
> Level: L1 acceptance — threads the timeZone foundation through DTO/command/User.register + persistence.
> Extends UserRegistrationIntegrationTest (no parallel class). see summaries/3.1-create-user-timezone.md
- [x] red-acceptance (UserRegistrationIntegrationTest — no RED, feature pre-existing; see summaries/3.1-create-user-timezone.md)
- [x] design (ADR create-user-timezone; see decisions/)
- [x] red-usecase (UserRegistrationServiceTest.when_commandHasTimeZone_expect_storedUserKeepsZone)
- [x] green-usecase (User stores command.timeZone)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (UserRepository.save built-in → [S] db; migration + DTO in green-acceptance)
- [x] green-acceptance (time_zone migration + RegisterUserRequest.timeZone; see summaries/3.1-create-user-timezone.md)

### Scenario E1 (promoted from Extended): Create with a duplicate email returns a field-level 422 (web-slice, L2)
> Level: L2 web-slice — promoted from Extended; mirrors Scenario 2.1 for the email field.
- [x] red-adapter rest (UserResourceTest.should_return422WithEmailFieldError_when_emailAlreadyExists)
- [S] design (mirrors 2.1 — no ADR)
- [x] green-adapter rest (EmailAlreadyExistsExceptionHandler → 422 + fieldErrors[email])
- [S] red-usecase (duplicate-email already covered at application level — UserRegistrationPolicy)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance (no L1 test for an error category)

## Integration Scenarios (06_Integration_Tests.md)
(none — create-user reuses the existing event → JWT → activation-email pipeline unchanged; activation
email is asserted as a side effect of backend Scenario 3.1)

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1.1: Sidebar shows an Admin Center group with a Users item
- [x] red-playwright (admin-center-nav.spec.ts)
- [S] red-frontend (static nav group — no .logic.ts seam)
- [S] green-frontend
- [S] red-frontend-api (no API call)
- [S] green-frontend-api
- [x] align-design (DashboardShell.vue sidebar — Admin Center group + Users item)
- [x] green-playwright
- [x] demo

### Scenario 1.2: Clicking Users navigates to the Users page inside the shell
- [x] red-playwright (users-navigation.spec.ts; UI-nav only)
- [S] red-frontend (declarative routing — no .logic.ts seam; see summaries/1.2-users-navigation.md)
- [S] green-frontend
- [S] red-frontend-api (no API call)
- [S] green-frontend-api
- [x] align-design (nested-route layout: /users child route; see summaries/1.2-users-navigation.md)
- [x] green-playwright
- [x] demo

### Scenario 2.1: Grid renders all columns and rows from the API
- [x] red-playwright (users-grid.spec.ts — 8 headers + per-row asserts)
- [x] red-frontend (users-grid.logic.test.ts — buildUserRows: status label, actor abbrev, full-name)
- [x] green-frontend (buildUserRows implemented)
- [x] red-frontend-api (admin-users.api.ts fetchAdminUsers + admin-users.schema.ts zod boundary)
- [x] green-frontend-api (fetchAdminUsers implemented)
- [x] align-design (UsersGrid.vue table-card grid; UsersPage orchestrator)
- [x] green-playwright
- [x] demo

### Scenario 2.2: Grid shows a loading state while fetching
- [x] red-playwright (users-grid.spec.ts Scn 2.2 — held-route loading pattern)
- [S] red-frontend (presentational loading ref — no .logic.ts seam)
- [S] green-frontend
- [S] red-frontend-api (reuses fetchAdminUsers)
- [S] green-frontend-api
- [x] align-design (UsersPage loading ref + centered spinner)
- [x] green-playwright
- [x] demo

### Scenario 3.1: Typing in a column filter narrows the rows client-side
- [x] red-playwright (users-grid.spec.ts Scn 3.1 — Full name filter; see summaries/3.1-column-filter.md)
- [x] red-frontend (filterRowsByFullName — case-insensitive contains + blank guard)
- [x] green-frontend (filterRowsByFullName implemented)
- [S] red-frontend-api (client-side filter — reuses fetchAdminUsers)
- [S] green-frontend-api
- [x] align-design (Full name filter input; displayedRows = filterRowsByFullName)
- [x] green-playwright
- [x] demo

### Scenario 3.2: Clicking a column header sorts the rows
- [x] red-playwright (users-grid.spec.ts Scn 3.2 — login asc/desc + status lifecycle sort)
- [x] red-frontend (sortUserRows — login localeCompare + status lifecycle rank)
- [x] green-frontend (sortUserRows + statusRank; see summaries/3.2-column-sort.md)
- [S] red-frontend-api (client-side sort — reuses fetchAdminUsers)
- [S] green-frontend-api
- [x] align-design (sortable Login/Status headers; displayedRows chains filter→sort)
- [x] green-playwright
- [x] demo

### Scenario 3.3: Timestamps show relative time with an absolute-on-hover tooltip
- [x] red-playwright (users-grid.spec.ts Scn 3.3; see summaries/3.3-relative-time-tooltip.md)
- [x] red-frontend (toRelativeTimeLabel + toAbsoluteTooltipParts; see summaries/3.3-relative-time-tooltip.md)
- [x] green-frontend (both implemented — floor cascade + Intl en-GB formatToParts)
- [x] red-frontend-api (current-user schema gains required timeZone; see summaries/3.3-relative-time-tooltip.md)
- [x] green-frontend-api (timeZone: z.string() + AuthenticatedUser.timeZone)
- [x] align-design (TimeCell.vue relative label + Teleport tooltip; UsersGrid viewerTimeZone prop)
- [x] green-playwright
- [x] demo

### Backend Foundation (deferred): GET /api/auth/me returns the viewer's timeZone (L1 acceptance)
> Closes the endpoints.md /me timeZone contract gap that Scn 3.3 green-frontend-api exposed (FE schema
> requires timeZone; live /me must emit it). USER DECISION: close after Scn 3.3, before 4.1.
> Scope: only the CurrentUserResponse DTO mapping (User already carries timeZone from 3.1).
> see summaries/3.3-relative-time-tooltip.md
- [x] red-acceptance (CurrentUserInfoIntegrationTest — "timeZone":"UTC" in whole-response expected)
- [S] design (trivial REST DTO field via ZoneId.getId())
- [S] red-usecase (AuthenticationService already returns User with timeZone)
- [S] green-usecase
- [S] red-domain (User.timeZone covered from 3.1)
- [S] green-domain
- [x] adapters-discovery (CurrentUserResponse.from simple delegation → [S] adapter)
- [x] green-acceptance (CurrentUserResponse.timeZone = user.getTimeZone().getId())

### Scenario 4.1: Register user opens a modal with the timezone pre-filled
- [x] red-playwright (register-user-modal.spec.ts Scn 4.1 — modal opens + timezone pre-fill exact-value)
- [S] red-frontend (presentational — modal open + fixed timezone; no .logic.ts seam; align-design)
- [S] green-frontend
- [S] red-frontend-api (no network request — modal open/prefill only; POST client at 5.1)
- [S] green-frontend-api
- [x] align-design (RegisterUserModal.vue + RegisterUserTextField.vue per mockup; UsersPage modalOpen wiring)
- [x] green-playwright
- [x] demo

### Scenario 4.2: Modal shows a loading state during submission
- [x] red-playwright (register-user-modal.spec.ts Scn 4.2 — submit spinner + all-fields-disabled)
- [S] red-frontend (presentational loading state — submitting ref; no .logic.ts seam; align-design)
- [S] green-frontend
- [x] red-frontend-api (create-user.api.ts createUser POST — MSW happy-path; CSRF handshake + 6-field body)
- [x] green-frontend-api (createUser POST via postJsonWithCsrf)
- [x] align-design (RegisterUserModal submitting ref + shared LoadingButton spinner; fields disabled)
- [x] green-playwright
- [x] demo

### Scenario 5.1: Successful create closes the modal and refreshes the grid
- [x] red-playwright (register-user-modal.spec.ts Scn 5.1 — success closes modal + grid refetch shows Pending row)
- [S] red-frontend (presentational orchestration — success closes modal + grid refresh; no .logic.ts seam; align-design)
- [S] green-frontend
- [S] red-frontend-api (createUser POST + grid refetch both pre-exist — Scn 4.2 / Scn 2.1; no new client)
- [S] green-frontend-api
- [x] align-design (RegisterUserModal emits `created` on success; UsersPage closes modal + refetches grid)
- [x] green-playwright (register-user-modal.spec.ts Scn 5.1 GREEN — 3/3 pass)
- [x] demo (register-user-modal.spec.ts Scn 5.1)

### Scenario 5.2: Duplicate login or email shows a field-level error
- [x] red-playwright (register-user-modal.spec.ts Scn 5.2 — duplicate-login 422 → login field error, modal stays open, values preserved)
- [x] red-frontend (create-user-error-view.logic.test.ts — mapCreateUserErrorToFieldErrors {login?,email?}; CreateUserError plumbing)
- [x] green-frontend (mapRegisterUserErrorToFieldErrors — login/email dispatch, unknown ignored)
- [x] red-frontend-api (register-user.api.test.ts — registerUser rejects RegisterUserError w/ fieldErrors on 422)
- [x] green-frontend-api (registerUser parses 422 problem+json → throws RegisterUserError; mirrors login.api)
- [x] align-design (RegisterUserTextField `error` prop → border-danger + .field-error; modal applyFieldErrors from mapper)
- [x] green-playwright (register-user-modal.spec.ts Scn 5.2 GREEN — 4/4 spec pass)
- [x] demo (register-user-modal.spec.ts Scn 5.2)

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

### Scenario 5.1: SQL injection in create fields is treated as literal text (db-adapter)
> Level: db-adapter (@DataJpaTest). Asserting a 422/201 at L1 proves nothing — JPA binds
> parameters literally regardless. Prove literal treatment at the repository: store a payload,
> look it up via findByX → empty + a control row that does match. Existence-check in red-adapter db:
> may already be covered by the Story 1 login-SQLi repository test → [S] if so.
- [ ] red-adapter db
- [ ] green-adapter db
- [S] design (literal-treatment is structural — JPA-parameterized; no design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance (repository-level proof per the SQLi pattern; no L1 test)

### Scenario 5.2: Stored XSS in a user name is escaped when rendered in the grid
- [ ] red-playwright
- [ ] red-frontend
- [ ] green-frontend
- [ ] red-frontend-api
- [ ] green-frontend-api
- [ ] align-design
- [ ] green-playwright
- [ ] demo

### Scenario 5.3: Mass assignment — extra fields on create are ignored (web-slice, L2)
> Level: L2 web-slice. RegisterUserRequest binds no role/status/id field, so extra JSON is ignored
> at the DTO boundary; PENDING status + server-generated id are guaranteed by the create path
> (already covered by 3.1). Verify at the web slice that injected fields don't reach the command.
> green-adapter rest is likely a no-op (the DTO is already structurally safe).
- [ ] red-adapter rest
- [ ] green-adapter rest
- [S] design (structural DTO safety — no design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance

### Scenario 5.4: Input length limits on create fields are enforced (web-slice, L2)
> Level: L2 web-slice. @Size bean-validation on RegisterUserRequest → 422 with field errors
> (same path as the existing beanValidationTest). green-adapter rest is [S] where @Size already
> covers the field (login/email/middleName); add @Size only where a field still lacks a limit.
- [ ] red-adapter rest
- [ ] green-adapter rest
- [S] design (bean-validation constraints — no design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance

### Scenario 5.5: Invalid timezone value is rejected (web-slice, L2)
> Level: L2 web-slice. Non-IANA timeZone → 422 field error for timeZone. Depends on the timeZone
> field added in 3.1. Design decides where validity lives: a TimeZone value object (domain) whose
> invalid value surfaces as a 422, vs a DTO-level constraint. red-domain/green-domain activate
> only if the chosen design introduces a TimeZone VO with testable branches.
- [ ] red-adapter rest
- [ ] design
- [ ] green-adapter rest
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance

### Scenario 5.6: POST /api/admin/users without a CSRF token returns 403 (L1 acceptance)
> Level: L1 acceptance (corrected from web-slice). CSRF lives in the global security filter chain
> and is rendered by ProblemDetailAccessDeniedHandler; the project tests it full-context
> (ActivateAccountCsrfIntegrationTest, AuthCsrfIntegrationTest) — a web slice cannot exercise the
> real filter chain. Mirror that for POST /api/admin/users → 403 RFC-9457 ProblemDetail. Verifies
> existing global behavior reaches the new endpoint, so green-acceptance is likely a no-op.
- [ ] red-acceptance
- [S] design (existing CSRF + access-denied handler — no new design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
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

### Scenario 1.1: Database unavailable during list returns 500 with a Problem Detail (L1 acceptance)
> Level: L1 acceptance — full-context resilience (real app + broken DB). The global exception
> handler already maps an unhandled DB failure to a 500 RFC-9457 ProblemDetail with no internal
> leak; this verifies that for the list read path. No usecase/domain work.
- [ ] red-acceptance
- [S] design (existing global exception handling — no new design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [ ] green-acceptance

### Scenario 2.1: Database recovery allows the list after an outage (L1 acceptance)
> Level: L1 acceptance — full-context resilience: real app, DB outage then recovery, list works
> again. Exercises the existing read path's behavior across an outage; no usecase/domain work.
- [ ] red-acceptance
- [S] design (existing read path — no new design)
- [S] red-usecase
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] adapters-discovery
- [ ] green-acceptance

## Extended (reviewed at each phase's Extended Gate; Story Completion Gate is the backstop)

> Never executed by /continue. Surfaced here so the Per-Phase Extended Gate (or Story Completion Gate) reviews them.

**API (tests/extended/01_API_Tests_Extended.md) — Backend Extended Gate DONE (2026-06-27)**
- [x] E1. Create with a duplicate email returns a field-level 422 (PROMOTED at user request → see "Scenario E1" in Backend Scenarios above)
- [S] E2. Activation updates the audit fields visible in the grid (reviewed — DEFERRED to improvements I7)
- [S] E3. List order is stable when two users share the same createdAt (reviewed — DEFERRED to improvements I8; tiebreaker already implemented)

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
