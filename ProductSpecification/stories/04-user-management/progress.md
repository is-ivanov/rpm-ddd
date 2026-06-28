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
- [x] adapters-discovery (Check 1 db: UserSummaryQuery.findAllForGrid() — non-trivial self-join view query (UserSummaryView + ActorView), not skippable per ADR → red/green-adapter db; Check 2: read-only list, no domain exceptions → [S]; Check 3: UserResource.listUsers simple-delegation GET, no validation → [S], handler + ListUsersService created in green-acceptance)
- [x] red-adapter db (JpaUserSummaryQueryTest @DataJpaTest; @Subselect view + self-join @ManyToOne (no ActorView — DuplicateMappingException); exclusion + ordering + actor resolution; RED on UnsupportedOperationException, prediction all-YES; ADR corrected)
- [x] green-adapter db (UserSummaryView made non-final + protected no-arg ctor (NullAway.Init suppressed on ctor) + explicit @Column on login/email to clear SpotBugs UWF_UNWRITTEN_FIELD; findAllForGrid = findByIdNot(SYSTEM_USER_ID, Sort createdAt/id DESC) + view→UserSummary mapping with system-actor special-case; db test GREEN 1/0/0; coverage clean (resolveActor both branches); spotbugs+checkstyle+pmd+NullAway green. NOTE: db test is a deletion candidate per ADR — revisit after green.)
- [x] green-adapter db (corrective): UserSummary projection was under-modeled (only userId + 2 ActorNames) vs the ADR/acceptance fixture (full grid row). Enriched UserSummary with name/login/email/status/createdAt/updatedAt (trivial pass-through, no new db test — Level 1 covers); toSummary maps them from UserSummaryView; db test narrowed two actor assertions from full-object eq to extracting(createdBy, updatedBy). db test GREEN; checkstyle+pmd+spotbugs green.
- [x] green-acceptance (ListUsersService + UserResource.listUsers handler + UserSummaryResponse.from mapper created under the simple-delegation plumbing exception; @ExpectedToFail removed. Surfaced + fixed 4 prerequisite gaps: (1) @EntityGraph(createdBy,updatedBy) fetch-join on the view repo — LazyInitializationException because actor @ManyToOne was LAZY and mapping ran outside a session; (2) TZ-naive seed timestamps in timestamptz interpreted in JVM TZ → SET TIME ZONE 'UTC' in db.changelog-test.xml (Liquibase rejects the offset-bearing 'Z' literal, so the fix is session-level not value-level); (3) ann_lee.middle_name empty-string → NULL in user.csv; (4) shared-DB pollution — full-context tests accumulate committed users so the read-all grid asserted 6 but saw 12: added baseline reset (@BeforeEach deletes non-seed/non-system iam_user) to AbstractApplicationIntegrationTest and DELETED the redundant JpaUserSummaryQueryTest per ADR (duplicated L1). Full suite 147/0/0; checkstyle+pmd+spotbugs green.)

### Scenario 2.1: Create with a duplicate login returns a field-level 422 (web-slice, Level 2)
> Duplicate login/email detection already exists & is tested at the application level
> (UserRegistrationPolicy + usecase tests). The only NEW behavior is the web-layer
> exception→HTTP mapping, so this is a web-slice (Level 2) scenario in UserResourceTest —
> NOT a Level-1 acceptance test (pyramid: acceptance = happy path only; the spec's own
> note in 01_API_Tests.md says per-status variations live in web-slice). Original
> bootstrapped red-acceptance/usecase/acceptance steps were the wrong level → corrected.
> Design (user, Option A): model the duplicate as a validation-failed field error —
> status 422, type=.../problem/validation-failed, fieldErrors[{code:ALREADY_EXISTS,
> property:login, message:"Login already exists", rejectedValue, path:login}].
- [x] red-adapter rest (UserResourceTest @WebTest: stub UserRegistrationService → LoginAlreadyExistsException; assert 422 + ProblemDetail + fieldErrors[login]; @ExpectedToFail; RED 422-vs-500 prediction all-YES; 2 run/0 fail/1 skip; test-review clean; EXISTING_LOGIN const removes param-always-same IDE warnings)
- [x] design (Option A — validation-failed field error; chosen by user, no ADR needed for a web-layer mapping)
- [x] green-adapter rest (LoginAlreadyExistsExceptionHandler: wim-deblauwe ApiExceptionHandler maps the domain exception → ApiErrorResponse(422, VALIDATION_FAILED) + ApiFieldError[ALREADY_EXISTS/login]; LoginAlreadyExistsException gains a login() accessor for the rejectedValue; test @Imports the handler into the @WebMvcTest slice (a plain @Component isn't slice-scanned); SpotBugs BC_UNCONFIRMED_CAST FP suppressed in exclude-filter. 2/0/0 green; coverage 100%; spotbugs/checkstyle/pmd green)
- [S] red-usecase (duplicate-login detection already implemented & tested at application level — UserRegistrationPolicy)
- [S] green-usecase (no new usecase logic)
- [S] red-domain
- [S] green-domain
- [S] green-acceptance (no Level-1 test for an error category — pyramid: acceptance = happy path only)

### Scenario 3.1: Create user with a timezone succeeds and appears in the grid (L1 acceptance)
> Level: L1 acceptance — happy path with genuine new backend work (threads the `timeZone`
> foundation through DTO/command/User.register + persistence), so the full backend sequence applies.
> Extends the existing registration acceptance test (UserRegistrationIntegrationTest) — add `timeZone`
> to the request and assert the new user is listed in the grid. Do NOT create a parallel acceptance class.
- [x] red-acceptance (no RED achievable — feature pre-existing: all create-action consequences (201, Location, activation email, grid PENDING, createdAt==updatedAt, createdBy==updatedBy==admin) already green via Story 1 registration + Scn 1.1 grid. timeZone is NOT L1-observable (PENDING user can't reach /me w/o activate+login = separate lifecycle → full-stack journey; grid carries no timeZone). Strengthened the existing UserRegistrationIntegrationTest to assert the new user flows into the grid (recursive-comparison full row) — a real new consequence on the same single action; committed green, no @ExpectedToFail. Added timeZone to RegisterUserRequest as nullable/ignored plumbing. DECISION (user): verify timeZone storage at L3 usecase, NOT via jdbcClient DB-peek in L1 (violates black-box rule). 1/0/0; test-review tightened to full-row eq; checkstyle/pmd/IDE clean.)
- [x] design (ADR create-user-timezone-decision: domain type = core-Java ZoneId (no bespoke VO); RegisterUserRequest keeps String @NotBlank @Size(64) + toCommand() ZoneId.of; User/command gain ZoneId timeZone; migration time_zone varchar(64) nullable→backfill 'UTC'→NOT NULL. Forward: 5.5 validity = web-slice jakarta constraint → 422, NOT a domain VO, so red/green-domain stay [S] in 3.1 & 5.5. Postgres has no zone-id type → varchar.)
- [x] red-usecase (UserRegistrationServiceTest.when_commandHasTimeZone_expect_storedUserKeepsZone — command carries ZoneId; User.register stores a fixed ZoneId.of("UTC") RED stub so the strict assert (expected America/New_York) fails; @ExpectedToFail(AssertionError.class); prediction all-YES; 3 run/1 fail→skip; test-review tightened the shared clock to a non-round instant 2026-04-30T12:34:17.482Z; refactor clean; checkstyle Javadoc added on RegisterUserCommand ctor + RegisterUserRequest.toCommand; pmd/checkstyle green. Plumbing only — no migration/persistence; ZoneId param unused on User.register is the deliberate GREEN seam.)
- [x] green-usecase (User constructor now stores the passed ZoneId instead of the hardcoded ZoneId.of("UTC") placeholder; register threads command.timeZone() through; @ExpectedToFail removed. UserRegistrationServiceTest 3/0/0 (XML report — surefire .txt shows 0 for @Nested, XML authoritative); spotless/checkstyle/pmd/spotbugs green; one-line assignment, no new branch → coverage trivially satisfied.)
- [S] red-domain
- [S] green-domain
- [x] adapters-discovery (Check 1 ports: UserRepository.save() = built-in Spring Data JPA save, no @Query → [S] no db-adapter test; required production plumbing for green-acceptance (simple-plumbing exception, mirrors Scn 1.1): Liquibase migration adding `time_zone varchar(64)` to iam_user (nullable→backfill 'UTC'→NOT NULL, mirroring 2026.06.27-01 audit) + include in changelog-cumulative; Hibernate maps ZoneId→VARCHAR natively (no AttributeConverter per ADR); seed user.csv + loadUpdateData need a time_zone value once NOT NULL. Check 2 exceptions: registerUser's Login/EmailAlreadyExists already mapped; ZoneId stored as-is, no NEW exception (invalid-zone is 5.5 web-slice) → [S]. Check 3 inbound REST: UserResource create endpoint is simple delegation; RegisterUserRequest.toCommand() must build ZoneId.of(timeZone) (currently hardcodes UTC) + field becomes @NotBlank @Size(64) per ADR — DTO conversion plumbing, no error-mapping for 3.1 (validity = 5.5) → [S], wiring created in green-acceptance. NO new red/green-adapter steps.)
- [x] green-acceptance (timeZone foundation plumbing per discovery, simple-plumbing exception: new migration 2026.06.27-02-changelog-iam-user-timezone.xml (add time_zone varchar(64) nullable → backfill 'UTC' → NOT NULL, mirroring audit) + include in changelog-cumulative; Hibernate maps timeZone→time_zone (snake_case strategy, no @Column/converter); seed user.csv gained a time_zone=UTC column (loadUpdateData auto-detects). RegisterUserRequest.timeZone @Nullable→@NotBlank + toCommand ZoneId.of(timeZone). UserRegistrationIntegrationTest 1/0/0. Collateral web-slice breakage from @NotBlank fixed (user-approved accept-as-is, required-field wiring): registerUser_beanValidation_out.json count 4→5 + timeZone error; UserResourceTest.validRegistrationRequest() gained "timeZone":"UTC"; ActivationTokenFixture null→"UTC" (posts via real HTTP @Valid). Full suite 145/0/0; spotless/checkstyle/pmd/spotbugs green.)

### Scenario E1 (promoted from Extended): Create with a duplicate email returns a field-level 422 (web-slice, L2)
> Promoted from tests/extended/01_API_Tests_Extended.md at user request (per-level extended gate).
> Mirrors Scenario 2.1 (duplicate login) for the email field: EmailAlreadyExistsException had NO
> web-layer mapping → fell through to the error-handling starter default (500 with a problem type but
> no field-level 422/fieldErrors). Web-slice (L2): red-adapter rest → green-adapter rest.
- [x] red-adapter rest (UserResourceTest.should_return422WithEmailFieldError_when_emailAlreadyExists: stub UserRegistrationService → EmailAlreadyExistsException; assert 422 + ProblemDetail + fieldErrors[email]; @ExpectedToFail(AssertionError.class); RED confirmed 500-vs-422, prediction all-YES; 3 run/0 fail/1 skip. Also fixed a latent mock-leak: auto-registered Mockito mocks are NOT reset between web-slice tests, so the existing login test broke on re-stub (given(mock.method()) invokes the already-throwing stub) → switched both duplicate-* stubs to willThrow(...).given(mock).method() which doesn't invoke during setup.)
- [S] design (mirrors 2.1 — validation-failed field error mapping; no ADR)
- [x] green-adapter rest (EmailAlreadyExistsExceptionHandler mirrors LoginAlreadyExistsExceptionHandler → 422 VALIDATION_FAILED + ApiFieldError[ALREADY_EXISTS/email]; EmailAlreadyExistsException gained an email() accessor for the rejectedValue; @ExpectedToFail removed; handler auto-discovered by WebTest @ComponentScan (no @Import). UserResourceTest 3/0/0; full suite 160/0/0; spotbugs cast-FP covered by the scoped *ExceptionHandler exclude-filter; checkstyle/pmd green.)
- [S] red-usecase (duplicate-email detection already implemented & tested at application level — UserRegistrationPolicy)
- [S] green-usecase
- [S] red-domain
- [S] green-domain
- [S] green-acceptance (no Level-1 test for an error category — pyramid: acceptance = happy path only)

## Integration Scenarios (06_Integration_Tests.md)
(none — create-user reuses the existing event → JWT → activation-email pipeline unchanged; activation
email is asserted as a side effect of backend Scenario 3.1)

## Frontend Scenarios (02_UI_Tests.md)

### Scenario 1.1: Sidebar shows an Admin Center group with a Users item
- [x] red-playwright (admin-center-nav.spec.ts + HomePageStatements sidebar locators/asserts: admin-center-group/users-nav-item test-ids, exact toHaveText "Admin Center"/"Users"; reuses CurrentUserBackendStatements.givenAuthenticatedUser. RED on the missing group label + Users item in DashboardShell's placeholder sidebar, prediction all-YES (locator→0 elements, toBeVisible timeout); test.fail() locked; test-review CLEAN; refactor no-op (idiomatic copy of existing page-object pattern); lint green; idea MCP get_file_problems unavailable → IDE inspection skipped.)
- [S] red-frontend (purely presentational static nav group — no branching/computation/validation/transformation; no .logic.ts seam, no existing nav logic to reuse: grep nav|sidebar|admin.center across frontend/src returned zero. "Admin Center" group + "Users" item are static markup added to DashboardShell.vue during align-design. Trivial-logic gate fails → no test written per the post-implementation trivial-test gate.)
- [S] green-frontend (no logic to implement — green counterpart of the trivial-logic [S] red-frontend)
- [S] red-frontend-api (no API call in this scenario — the gherkin is a static nav group + item with no data fetch. Zero production files in the frontend-api layer; the shell's /me call uses the pre-existing current-user.api.ts from prior stories, no NEW API client needed. Existence/applicability skip per the skip-validation rule.)
- [S] green-frontend-api (no API client to implement — green counterpart of the [S] red-frontend-api)
- [x] align-design (DashboardShell.vue sidebar rebuilt from placeholder → real nav matching mockup 01-users-grid.html: Home nav-item (active), "Admin Center" nav-group-label, "Users" sub nav-item (pl-8). test-ids home-nav-item/admin-center-group/users-nav-item; exact-text "Admin Center"/"Users" satisfied (icon SVG contributes no text). style.css: added --color-sidebar-hover/#2d3139 + --color-sidebar-active/#3b4252 tokens + extracted .nav-group-label/.nav-item/.nav-item-active component classes (repeated/opaque-chain extraction per tailwind binding). design-review PASS (no placeholder data leaked — names/emails/dates correctly omitted). refactor no-op (clean). verify: value-level match to mockup confirmed. coverage N/A (green-frontend/green-frontend-api both [S], no testable logic — pure presentational, covered E2E at green-playwright). build+lint(oxlint/eslint/prettier/type-check)+57/57 unit green; IDE inspections clean on both files.)
- [x] green-playwright (removed test.fail() + stale RED comment from admin-center-nav.spec.ts; frontend-only test — /api/auth/me mocked via page.route (CurrentUserBackendStatements), no real backend needed; Playwright webServer auto-started Vite. 1 passed (3.9s); remove-marker-only, no production/Statements changes. lint(oxlint/eslint/prettier/type-check) green; IDE inspection clean.)
- [x] demo (recorded admin-center-nav E2E in headless slowMo=2000 + video on; 1 passed (6.9s); recording → frontend/test-results/demo-admin-center-nav.webm (gitignored); playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 1.2: Clicking Users navigates to the Users page inside the shell
- [x] red-playwright (users-navigation.spec.ts in new acceptance/tests/frontend/users/ dir + UsersPageStatements (users-page, register-user-button) + HomePageStatements.clickUsersNavItem(). UI-nav only (sidebar click, never URL); one-page-object-per-page — Users page asserts its own content + reuses homePage shell-chrome asserts (dashboard-shell + topbar-logo + sidebar), no middleman. RED: static users-nav-item link has no nav wired, no /users route/page → click leaves user on dashboard, users-page/register-user-button never render → assertUsersPageIsVisible() toBeVisible timeout (getByTestId 0 elements, 5000ms); prediction all-YES. test.fail() locked (1 passed RED-state). test-review tightened: added assertTopbarLogoIsVisible() (spec says "inside the same top bar AND sidebar shell"). refactor no-op (idiomatic, sibling-consistent, all <200 lines). lint+IDE clean. Story scenario → no issue tag.)
- [S] red-frontend (in-shell navigation is declarative routing — no .logic.ts seam: clicking Users is unconditional nav (no branching/computation/validation/transformation), the Users page + "Register user" button are static markup, and this scenario fetches no data. Existing nav pattern is a direct component-level router.push (UserMenu.vue `router.push('/')`) with no logic helper; grep router.push|router-link|navigate across frontend/src found zero navigation .logic.ts. Trivial-logic gate fails → no test written. Architectural choice for green-frontend/align-design: nested-route layout — extract the shell from HomePage into a layout hosting a nested <router-view> with `/` (Home content) + a new `/users` child route, so both render inside the shared top bar + sidebar; Users sidebar item becomes a router-link / @click router.push('/users'). Either nested-route or an activeSection view-state ref keeps navigation presentational.)
- [S] green-frontend (no logic to implement — green counterpart of the trivial-logic [S] red-frontend; the route/layout + Users page component are built in green-frontend/align-design as presentational markup, covered E2E by the already-RED users-navigation.spec.ts)
- [S] red-frontend-api (no API call in this scenario — clicking Users navigates to the Users page; it fetches no data (the grid data load is Scenario 2.1). Zero production files in the frontend-api layer, no new API client. Existence/applicability skip per the skip-validation rule.)
- [S] green-frontend-api (no API client to implement — green counterpart of the [S] red-frontend-api)
- [x] align-design (USER-APPROVED architecture = Option A nested-route layout, route /users for the users grid. Built: DashboardShell.vue main → <RouterView> + sidebar <a> → <RouterLink> with route-name active state (navItemClasses helper); new DashboardHome.vue (extracted Home content: page-title+placeholder) as child route ''; new features/users/components/UsersPage.vue (users-page + Register user button per mockup content-head, inline toolbar button — not the w-full form .btn-primary); router nested children ''→DashboardHome / users→UsersPage under /→HomePage (auth-gating unchanged). home.smoke.test.ts re-pointed to mount App+router with the nested home child (assertions unchanged, 57/57). refactor: extracted .page-title (dup h1 chain). design-review PASS (no grid placeholder data leaked — grid deferred to 2.1). verify: mockup match confirmed. coverage: navItemClasses both branches hit by smoke test; rest presentational/E2E. No regression — home+routing E2E 9/9 green; users-navigation.spec.ts now "expected-fail-but-passed" (impl works, marker flips in green-playwright). build+lint+57/57 vitest green; IDE clean on all code files.)
- [x] green-playwright (removed test.fail() + RED comment from users-navigation.spec.ts; frontend-only test — /api/auth/me mocked via page.route (CurrentUserBackendStatements), no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` (project carries baseURL/webServer); 1 passed (4.2s). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [x] demo (recorded users-navigation E2E in headless slowMo=2000 + video on; 1 passed (7.9s); recording → frontend/test-results/demo-users-navigation.webm (gitignored). FE-only (page.route mocks, no backend). playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 2.1: Grid renders all columns and rows from the API
- [x] red-playwright (users-grid.spec.ts + AdminUsersBackendStatements (page.route '**/api/admin/users' → bare JSON array) + admin-users-fixture.ts (SEVERAL_ADMIN_USERS API-shape + EXPECTED_USER_ROWS display values, 4 rows createdAt DESC, non-round ts, all 4 statuses + System/J.Doe actors); extended UsersPageStatements with grid asserts (8 headers exact toHaveText, toHaveCount rows, per-row name/login/email + status badge + abbreviated actor + seed "System"). Nav via UI (homePage.clickUsersNavItem), backend mock via injected backend Statements. RED: UsersPage.vue has only heading+button, no grid → getByTestId('users-grid') 0 elems → toBeVisible timeout 5000ms; prediction all-YES; test.fail() locked (1 passed expected-failure). test-review CLEAN (0 edits — all deterministic values already strict). refactor: de-branched assertSeedActorIsShownAsSystem via fixture-derived SEED_ACTOR_CELLS + ACTOR_CELL lookup (move-behavior-to-data, no assertion deleted), unified loop idiom. Timestamps NOT asserted (relative time = Scn 3.3). lint(oxlint/eslint/prettier/type-check)+IDE clean; all files <200. Story scenario → no issue tag.)
- [x] red-frontend (trivial-logic gate PASSED — 3 non-trivial transformations with branching: status code→label (ACTIVE→Active…), audit actor abbreviation {first,mid,last}→"J. Doe" + System special-case (empty lastName)→"System" verbatim, full-name composition with optional middle. New users-grid view-model triplet under features/users/logic: users-grid.types.ts (PersonName/UserAudit/UserSummaryResponse mirroring backend + UserRow VM; createdAt/updatedAt raw ISO passed through for Scn 3.3) + users-grid.logic.ts (buildUserRows RED stub = raw pass-through, GREEN seam) + __tests__/users-grid.logic.test.ts (8 it.fails: 4 status labels via it.fails.each, actor abbrev, System verbatim, name with/without middle; self-contained fixtures in lock-step with acceptance EXPECTED_USER_ROWS; login/email pass-through NOT asserted per trivial-test gate). Pattern mirrors app-version triplet (VM+API type in .types.ts). RED: stub returns raw values → AssertionError; prediction all-YES (8 cases); it.fails locked (8 expected-fail, build green). System-case RED driven by updatedBy "J. Doe" assert (createdBy==='System' passes today by coincidence = green-guard against naive "S. " abbreviation). test-review CLEAN (strict toBe, no identity asserts, System pin verified). refactor clean no-op (idiomatic, app-version-consistent). lint(oxlint/eslint/prettier/type-check)+IDEA clean; all files <200. Story scenario → no issue tag.)
- [x] green-frontend (buildUserRows implemented — was a raw pass-through stub: toStatusLabel Record lookup ACTIVE/PENDING/LOCKED/INACTIVE→Active/Pending/Locked/Inactive (raw-code fallback for unmapped); toFullName = [first,middle,last].filter(Boolean).join(' ') (null middle drops out → "First Last", present → "First Middle Last"); toActorLabel = empty lastName renders firstName verbatim ("System"), else "{firstInitial}. {lastName}" — applied to both createdBy & updatedBy; login/email/createdAt/updatedAt pass through (raw ISO; relative-time is Scn 3.3). 5 it.fails/it.fails.each flipped to it/it.each, no assertion/fixture touched. 36 lines, no types change. Target 8/0/0; full FE unit suite 65/0/0. /refactor clean no-op (object-literal mapper is not a smell; status fallback + actor branch read cleanly; Humble Object pure). lint(oxlint/eslint/prettier/type-check) green; IDE inspections clean on both files.)
- [x] red-frontend-api (admin-users.api.ts fetchAdminUsers() RED stub (Promise.reject 'Not implemented') + admin-users.schema.ts zod schema validating UserSummaryResponse[] at the boundary (personName/userAudit sub-schemas) + __tests__/admin-users.api.test.ts MSW happy-path it.fails: stub GET /api/admin/users → 200 with a deterministic 2-user array, assert toEqual full parsed array. RED: stub rejects 'Not implemented' → result is Error ≠ array → AssertionError; prediction all-YES; it.fails locked (1 expected fail, build green). Reuses UserSummaryResponse from users-grid.types.ts (no type redefine). test-review CLEAN (strict whole-array toEqual, VITE_API_URL not hardcoded host, boundary-contract not identity). refactor no-op (stubs minimal/idiomatic, mirrors current-user.api/schema/test). Lint fix: async-without-await stub → non-async Promise.reject (oxlint require-await). lint(oxlint/eslint/prettier/type-check)+IDE clean; all files <200. Story scenario → no issue tag. NOTE for a later logic-layer refactor: vue-ts binding wants schema as the single source of truth via z.infer — users-grid.types.ts keeps parallel interfaces; collapsing them is a cross-layer change deferred out of this frontend-api step.)
- [x] green-frontend-api (fetchAdminUsers implemented — replaced the Promise.reject RED stub with fetch(apiUrl('/api/admin/users'), {GET, credentials:'include'}) → adminUsersSchema.parse(await response.json()) returning UserSummaryResponse[]; mirrors current-user.api.ts; boundary validation via the existing zod schema (no trust-cast). Only test change = it.fails→it (no assertion/fixture/stub touched). Target 1/0/0; full FE unit suite 66/0/0 no regressions. /refactor no-op (minimal idiomatic client, schema+test well-factored, all <200). lint(oxlint/eslint/prettier/type-check)+IDE clean. Cross-layer z.infer source-of-truth smell (users-grid.types.ts parallel interfaces) noted but deferred — logic-layer change, out of frontend-api scope.)
- [x] align-design (UsersGrid.vue built — table-card grid matching mockup 01-users-grid.html: 8 headers (Full name/Login/Email/Status/Created/Created by/Updated/Updated by, exact text), users-grid-row per API user, status badge + abbreviated audit actors + "System" seed actor; UsersPage.vue became orchestrator (onMounted → void loadUsers = buildUserRows(await fetchAdminUsers()), passes rows to <UsersGrid>). SCOPE: grid DISPLAY only — filter row (3.1), sort indicators (3.2), relative-time tooltips (3.3) intentionally NOT built; Created/Updated cells render raw ISO (3.3 formats them). style.css: +tokens success-surface/warning/warning-surface/column; extracted .table-card/.grid-head-cell/.grid-cell/.status-badge/.status-* component classes (repeated-utility + semantic-badge extraction). design-review PASS (every cell binds an API field; no mockup placeholder leaked). refactor CLEAN no-op (UsersGrid 69 / UsersPage 34 / style.css 140 lines; statusBadgeClass already extracted; tbody last-child variant + table-card shadow assessed acceptable). align-design verify: Playwright passes all exact-text header/cell + badge assertions ("expected-fail-but-passed" — marker flips in green-playwright). coverage: provider @vitest/coverage-v8 not installed (N/A) — buildUserRows already covered by 8 green-frontend unit tests, presentation covered E2E. build+lint(oxlint/eslint/prettier/type-check)+66/66 vitest green.)
- [x] green-playwright (removed test.fail() + RED comment from users-grid.spec.ts; frontend-only test — /api/auth/me + /api/admin/users mocked via page.route (CurrentUserBackendStatements + AdminUsersBackendStatements), no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` from frontend/ (config testDir ./acceptance); 1 passed (2.0s). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [x] demo (recorded users-grid E2E in headless slowMo=2000 + video on; 1 passed (6.1s); recording → frontend/test-results/demo-users-grid.webm (gitignored). FE-only (page.route mocks, no backend). playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 2.2: Grid shows a loading state while fetching
- [x] red-playwright (users-grid.spec.ts +Scn2.2 test in existing describe; held-route loading pattern: AdminUsersBackendStatements.givenAdminUserListInFlight() (route handler awaits a release promise — no fixed sleep) + releaseAdminUserList(); UsersPageStatements +loading testid `users-grid-loading` + assertLoadingStateIsVisible (indicator visible & rows toHaveCount(0) while in-flight) + assertRowsRenderAfterResponse (indicator gone + grid visible + rows toHaveCount(4) after release). Nav via UI (clickUsersNavItem), backend mock injected. RED: UsersPage.vue has no loading markup → getByTestId('users-grid-loading') 0 elems → toBeVisible timeout 5000ms; prediction all-YES (Type/Message/Status). test.fail() locked (1 passed expected-failure) + 2 RED-reason comments. test-review CLEAN (strict assertions confirmed; 1 prettier line-wrap fix only). refactor CLEAN no-op (mirrors Scn 2.1 siblings; fulfillAdminUserList shared, release flag mirrors current-user sessionEnded idiom). lint(oxlint/eslint/prettier/type-check) exit 0; IDE inspections clean on all 3 files; all <200. Story scenario → no issue tag.)
- [~] red-frontend
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
