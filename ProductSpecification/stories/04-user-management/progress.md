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
- [S] red-frontend (trivial-logic gate FAILED on all 4 triggers — loading state is presentational component-local state, not a `.logic.ts` seam: a `loading` boolean ref toggled true-before-fetch / false-in-finally around the existing `loadUsers()`. No branching/computation/validation/transformation; the only data transform `buildUserRows` already exists & is green from Scn 2.1. Per frontend-rules "Async Action Buttons (Loading State)" the in-flight flag is presentational state handled in the component during align-design. red-agent confirmed.)
- [S] green-frontend (counterpart of [S] red-frontend — no `.logic.ts` to implement; the loading ref + spinner markup are built in align-design, verified E2E by red/green-playwright)
- [S] red-frontend-api (existence/skip-validation: Scn 2.2 makes NO new API call — it reuses the existing `fetchAdminUsers()` (admin-users.api.ts) + zod schema from Scn 2.1, observed in-flight only. Zero production files in the frontend-api layer change → skip per the skip-validation rule.)
- [S] green-frontend-api (counterpart of [S] red-frontend-api — no API client to implement)
- [x] align-design (UsersPage.vue gained the loading state per mockup desktop/05-grid-loading.html: `loading` ref starts true, toggled in loadUsers() try/finally; template renders `v-if="loading"` → centered spinner (LoaderCircle :size=32 animate-spin text-accent, wrapped `flex h-90 items-center justify-center`, data-testid="users-grid-loading") in place of the grid, `v-else` → <UsersGrid>. Mockup fidelity: 32px / accent / centered / 360px panel (h-90 = 90×4px). Spinner uses bare `animate-spin` (1s) consistent with AppLoading/LoadingButton/AppVersion rather than the mockup's 0.8s — deliberate cross-component consistency. design-review PASS (no placeholder data — loading shows only a spinner; reachable state confirmed). refactor CLEAN no-op (43 lines; loading toggle is presentational orchestration, correct location; `flex h-90 …` single-use self-documenting → no CSS extraction; spinner byte-identical to AppLoading). align-design verify: build green; Playwright Scn 2.2 = "expected to fail, but passed" (impl works, marker flips in green-playwright). coverage: provider installed; UsersPage.vue uncovered lines are presentational (E2E-covered by Scn 2.2 Playwright), buildUserRows unchanged & already covered → no reachable unit gap, no new steps. lint(oxlint/eslint/prettier/type-check) exit 0; IDE inspection clean. NOTE: size-32 accent spinner now duplicated in AppLoading.vue + UsersPage.vue — extract a shared AppSpinner on the 3rd usage.)
- [x] green-playwright (removed test.fail() + 2 stale RED comments from users-grid.spec.ts Scn 2.2; frontend-only test — /api/auth/me + /api/admin/users mocked via page.route (CurrentUserBackendStatements + AdminUsersBackendStatements held-route), no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` from frontend/; 1 passed (1.9s). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [x] demo (recorded users-grid Scn 2.2 loading-state E2E in headless slowMo=2000 + video on; 1 passed (8.7s); recording → frontend/test-results/demo-users-grid-loading.webm (gitignored). FE-only (page.route mocks, no backend). playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 3.1: Typing in a column filter narrows the rows client-side
- [x] red-playwright (Scn 3.1 added to users-grid.spec.ts: types "ar" into the Full name filter (`data-testid=users-filter-name`), asserts only "Sarah Jane Connor" + "Emily Carter" rows survive — strict ordered `toHaveText` array + `toHaveCount(2)` by identity not index — AND that `/api/admin/users` was fetched exactly once (request counter in AdminUsersBackendStatements.assertAdminUserListRequestedOnce → `toBe(1)`, the heart of "client-side filter, no refetch"). UsersPageStatements gained assertFullNameFilterIsVisible/enterFullNameFilter/assertOnlyMatchingFullNamesRemain; filter term + matching-names set live in admin-users-fixture.ts (FULL_NAME_FILTER_TERM + FULL_NAMES_MATCHING_FILTER **derived** from EXPECTED_USER_ROWS via .filter(includes), lock-step w/ row data like SEED_ACTOR_CELLS). RED locked with `test.fail()`; reason pinned via bounded assertFullNameFilterIsVisible (5s toBeVisible on the missing input) so an incidental whole-test timeout isn't absorbed as expected-fail. Prediction all-YES (locator→0 elems, toBeVisible timeout). 3 passed (Scn 3.1 = expected ✘). QUIRK: `test.fail()` absorbs thrown assertion/errors but NOT a 30s whole-test timeout (first fill()-only attempt failed the build red) → always front a bounded visibility assertion. test-review CLEAN; refactor moved filter constants to fixture (derived). red-frontend-api stays [S] (reuses fetchAdminUsers, no new client); downstream red-frontend IS a real .logic.ts "contains" predicate (non-trivial). lint(oxlint/eslint/prettier/vue-tsc)+IDE clean; all files <200. Story scenario → no issue tag.)
- [x] red-frontend (filterRowsByFullName(rows, term) added to users-grid.logic.ts as a RED pass-through stub (`void term; return rows;` — green seam preserved, lint-clean via void); users-grid.logic.test.ts +describe('Full name column filter') with 2 it.fails + 1 plain it: (1) term "ar" → strict ordered toEqual ['Sarah Jane Connor','Emily Carter'] (lock-step w/ FULL_NAMES_MATCHING_FILTER), (2) term "AR" pins case-insensitive seam (same 2 survivors; naive case-sensitive includes → [] fails), (3) blank "   " → all 4 names unchanged (real green guard: ''.includes drops every row). Fixtures = buildUserRows of the 4 acceptance names. RED: stub returns all 4 → AssertionError; prediction all-YES (verified via throwaway flip). 9 passed/2 expected-fail/0 skip, build green. test-review CLEAN (strict ordered toEqual throughout, no identity/contains/length). refactor CLEAN no-op (40-line logic / 116-line test, idiomatic, Humble Object pure). lint(oxlint/eslint/prettier/vue-tsc) green; IDE inspections clean on both files; all <200. Story scenario → no issue tag.)
- [x] green-frontend (filterRowsByFullName implemented — replaced the RED `void term; return rows;` pass-through: `const needle = term.trim().toLowerCase(); if (needle==='') return rows; return rows.filter(r => r.name.toLowerCase().includes(needle))`. Trim+lowercase both sides = case-insensitive contains; blank-term guard returns the same `rows` ref unchanged (load-bearing: `''.includes` would otherwise spuriously keep-all into a new array); filter preserves render order, no sort. 2 it.fails→it flipped (no assertion/fixture/expected touched), RED comments stripped. Target 11/11/0; full FE unit suite 69/0/0 no regressions. /refactor CLEAN no-op (44 lines, idiomatic; needle local names a real 2-step computation; single call site → no shared lowercase helper per repetition-trigger). lint(oxlint/eslint/prettier/vue-tsc) green; IDE inspections clean on both files.)
- [S] red-frontend-api (skip-validation PASSED — Scn 3.1 makes NO new API call: the column filter is purely client-side, narrowing the already-fetched rows via filterRowsByFullName (logic layer, green from this scenario). The only API call is the existing fetchAdminUsers() (admin-users.api.ts) + zod schema, reused unchanged from Scn 2.1; the filter term is never sent to the server. Zero production files in the frontend-api layer change → skip per the skip-validation rule. Pre-declared in red-playwright/red-frontend.)
- [S] green-frontend-api (counterpart of [S] red-frontend-api — no API client to implement)
- [x] align-design (Full name column filter built into UsersGrid.vue per mockup 01-users-grid.html `.filter-row`: a 2nd thead `<tr>` of `.filter-cell` band cells, the Full name column carrying a functional `<input data-testid="users-filter-name" class="filter-input" placeholder="contains">` bound `v-model="nameFilter"`; `displayedRows` computed = `filterRowsByFullName([...props.rows], nameFilter.value)` (logic-layer Humble Object, green from this scenario) drives the row `v-for` → purely client-side narrowing, no refetch. Filter state lives in UsersGrid (owns columns/rows display); UsersPage unchanged. SCOPE (per-scenario, matching Scn 2.1's deferral): only the Full name filter is wired — other filter cells render as empty band cells; Status multi-select (Extended E1) + Created/Updated date-range (Extended E3) + other column "contains" inputs intentionally NOT built. style.css: extracted `.filter-cell` (band: bg-column, px-4 py-2, border) + `.filter-input` (h-30/px-2/text-13, border-line, focus accent-focus + 3px ring — opaque rgba ring extracted per CSS rule) mirroring the mockup. design-review PASS (every cell binds API `row.*`; `placeholder="contains"` is a generic affordance, not user data — no placeholder leaked). refactor CLEAN no-op (UsersGrid 88 lines; filter row reuses the single COLUMNS source; displayedRows delegates to logic; `[...props.rows]` spread kept local — not cascaded into the logic signature per stay-in-layer). align-design verify: Playwright Scn 3.1 = "expected-fail-but-passed" (impl works, marker flips in green-playwright); Scn 2.1/2.2 still pass (no regression). coverage: users-grid.logic.ts filterRowsByFullName 100% (term ar/AR/blank from green-frontend); line-11 `?? status` fallback is pre-existing Scn 2.1 code, not this scenario; UsersGrid.vue presentational → E2E-covered. No new reachable unit gap. lint(oxlint/eslint/prettier/type-check) exit 0; full FE unit suite 69/0/0; all files <200.)
- [x] green-playwright (removed test.fail() + 4-line RED comment from users-grid.spec.ts Scn 3.1; frontend-only test — /api/auth/me + /api/admin/users mocked via page.route (CurrentUserBackendStatements + AdminUsersBackendStatements held/bare-array), no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` from frontend/; Scn 3.1 1 passed (2.0s); full users-grid file 3/3 green (no regression to 2.1/2.2). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [x] demo (recorded users-grid Scn 3.1 Full-name-filter E2E in headless slowMo=2000 + video on; 1 passed (7.9s); recording → frontend/test-results/demo-users-grid-name-filter.webm (gitignored). FE-only (page.route mocks, no backend). playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 3.2: Clicking a column header sorts the rows
- [x] red-playwright (Scn 3.2 added to users-grid.spec.ts: click `users-grid-header-login` → strict ordered `toHaveText` login cells ascending (`LOGINS_ASCENDING`), 2nd click → descending; click `users-grid-header-status` → status badges in lifecycle order Pending→Active→Locked→Inactive (`STATUSES_IN_LIFECYCLE_ORDER`). New focused `users-grid-sort.statements.ts` (clickLoginHeader/clickStatusHeader/assertLoginsSortedAscending/Descending/assertStatusesSortedByLifecycleOrder) injected directly — kept UsersPageStatements <200 (172). Fixture gained LOGINS_ASCENDING/DESCENDING (derived plain localeCompare via toSorted/toReversed) + STATUSES_IN_LIFECYCLE_ORDER (**explicit literal** — the lifecycle order IS the business rule, never derived by replicating the sort). RED: clicking Login header is a no-op (no sort wiring in UsersGrid.vue/.logic.ts) → ascending toHaveText timeout 5000ms, Expected [d.lee,e.carter,m.scott,s.connor] vs render-order [s.connor,m.scott,e.carter,d.lee]; prediction all-YES; bounded assertGridIsVisible() fronts the test (no 30s whole-test-timeout absorption). test.fail() locked; 4 passed (3.2 = ✘ expected-fail). test-review FIXED a production-mirroring lifecycle derivation → explicit literal. refactor CLEAN no-op (sibling grid-locator dup KEPT — reuse would force a forbidden middleman/base-class for a 3-line locator). NOTE green-frontend: descending-login coincides with createdAt-DESC render order so the desc assert alone wouldn't catch RED — asc + status asserts drive RED. lint(oxlint/eslint/prettier/vue-tsc)+IDE clean; all files <200. Story scenario → no issue tag.)
- [x] red-frontend (sortUserRows(rows, column, direction) RED pass-through stub added to users-grid.logic.ts + SortColumn 'login'|'status' / SortDirection 'asc'|'desc' in users-grid.types.ts; new describe('Column header sort') with 3 it.fails: Login asc (localeCompare), Login desc (reversed), Status lifecycle order ['Pending','Active','Locked','Inactive'] hand-listed literal — NOT derived, fails for alphabetical Active/Inactive/Locked/Pending. Self-contained fixture fed in deliberately-unsorted order [m.scott,s.connor,d.lee,e.carter] so all 3 directions are genuinely RED — the acceptance fixture's createdAt-DESC render order coincidentally equals Login-desc, which would have made a pass-through desc test pass. Lock-step on logins/one-row-per-status with the acceptance fixture. RED: stub returns input order → AssertionError; prediction all-YES (6 field rows). it.fails locked: 11 passed/3 expected-fail/0 skip. test-review CLEAN (strict ordered toEqual, lifecycle literal verified vs alphabetical, RED pinned). refactor CLEAN no-op (idiomatic, sibling-consistent, all <200). SortColumn narrowed to the 2 columns the scenario exercises — widen in a later scenario. lint(oxlint/eslint/prettier/vue-tsc)+IDEA clean. Story scenario → no issue tag.)
- [x] green-frontend (sortUserRows implemented — replaced the RED `void column; void direction; return rows;` pass-through with a non-mutating stable `rows.toSorted((a,b) => factor * compareByColumn(a,b,column))`: factor = direction==='desc' ? -1 : 1; compareByColumn dispatches login→localeCompare vs status→statusRank subtraction. Lifecycle rank is an explicit data-driven map Pending(0)/Active(1)/Locked(2)/Inactive(3) on the display label — NOT alphabetical. 3 it.fails→it flipped (no assertion/fixture/expected touched), RED comments stripped. Full FE unit suite 72/0/0 (target file 14/14, +3 sort), no regressions. /refactor CLEAN no-op (63 lines, intention-revealing names, comparator idiomatic; rejected unifying STATUS_LABELS+STATUS_LIFECYCLE_RANK into one code→{label,rank} map — crosses into Scn 3.1 toStatusLabel, out of green-frontend layer). lint(oxlint/eslint/prettier/vue-tsc)+IDEA clean; all files <200. POST-GREEN hardening (user architecture decision 2026-06-29, option A: FE owns the lifecycle order + labels, no API contract change): added statusRank() with UNKNOWN_STATUS_RANK=MAX_SAFE_INTEGER fallback so an unknown status (a BE code the FE doesn't map yet) sorts to the end instead of yielding NaN via undefined−number and silently breaking the sort; new RED→GREEN test (4-row fixture incl. status 'SUSPENDED', expects [Pending,Active,Locked,SUSPENDED]). Rationale: lifecycle order is a domain fact (source of truth = UserStatus enum) duplicated on FE because the grid sorts client-side (no server paging/sort — Story 4 decision) and the wire contract sends only the bare status code; this duplicates STATUS_LABELS too. Single-source-of-truth (BE order()/statusOrder in response) deferred to improvement I10 (needs ADR + contract change); NaN fallback is the interim safeguard. Full FE suite 73/0/0 (+1 test); lint clean; IDE MCP unavailable → inspections skipped.)
- [S] red-frontend-api (skip-validation PASSED — Scn 3.2 makes NO new API call: column-header sort is purely client-side, reordering the already-fetched rows via sortUserRows(rows, column, direction) in users-grid.logic.ts (logic layer, GREEN from this scenario). The only API call is the existing fetchAdminUsers() (admin-users.api.ts, unchanged from Scn 2.1) + adminUsersSchema (zod) — which already validate BOTH sort columns (login + status as z.string() in personNameSchema-rooted array), so no schema extension is needed either. The sort column + direction are presentational client-side state, never serialized into a request or sent to the server (diagnostic: remove the frontend-api layer entirely and sorting still works — it reads only the in-memory UserRow[]). Existence check PASSED (fetchAdminUsers + schema provide the data); trivial-logic check PASSED (no API transformation); ZERO production files in the frontend-api layer change → skip per the skip-validation rule. Mirrors Scn 3.1's [S] red-frontend-api — identical client-side-only pattern.)
- [S] green-frontend-api (counterpart of [S] red-frontend-api — no API client to implement; the sort lives in the logic layer, verified E2E by the already-RED users-grid.spec.ts Scn 3.2)
- [x] align-design (Sort UI built into UsersGrid.vue per mockup 01-users-grid.html `.th-sort`: Login + Status headers became clickable (click handler on the `<th>` since the test clicks the testid-bearing `<th>`), toggling a `sort: Ref<SortState | null>` — null (backend createdAt-DESC order) → click → asc → click → desc → click → asc; clicking a different column switches to it at asc. Icon via `<component :is>` dispatching lucide ChevronsUpDown (unsorted, opacity-60) / ArrowUp (asc) / ArrowDown (desc), both sorted states → opacity-100 text-accent (mirrors mockup `.th-sort i` opacity 0.6 / `.th-sort.sorted i` opacity-1 accent). `displayedRows` computed now chains filterRowsByFullName → sortUserRows (both logic-layer, green). SCOPE (per-scenario, matching Scn 3.1's filter-row discipline + the narrowed SortColumn='login'|'status' in logic + the test): ONLY Login + Status headers are sortable — the other 6 render plain (no misleading "looks clickable but dead" affordance); their sortability is deferred to a later scenario/extended case (the mockup's all-8-sortable is the target end-state, built incrementally). style.css: extracted `.grid-head-cell-sortable` (cursor-pointer on the th), `.th-sort` (inline-flex items-center gap-1), `.th-sort-icon` (h/w-14px opacity-60), `.th-sort-sorted .th-sort-icon` (text-accent opacity-100) — mockup's `.th-sort`/`.th-sort i`/`.th-sort.sorted i` translated to the project's @apply + --color-* token convention. design-review PASS (every cell still binds an API row.* field; labels come from the existing COLUMNS array, not mockup placeholder data; icons are lucide components, no inline SVG). refactor CLEAN no-op (UsersGrid 137 lines; onHeaderClick guard-then-toggleSort is normal component event-handling, not a middleman; sortIconFor/isActiveSort single-use per-header mappers; `[…props.rows]` spread kept local per stay-in-layer). align-design verify: build green; Playwright users-grid 2.1/2.2/3.1 ✓ no regression, Scn 3.2 = "expected to fail, but passed" (sort works — login asc/desc + status lifecycle all correct; test.fail() flips in green-playwright). coverage: users-grid.logic.ts 100% (15 unit tests incl. sortUserRows + statusRank unknown-status); UsersGrid.vue 0% unit = presentational component (sort ref + toggleSort/sortIconFor reactive state + event handlers, all 4 transition branches E2E-reached by Scn 3.2), consistent with Scn 2.1/2.2/3.1 presentational-component pattern (loading/nameFilter refs). Extracting nextSortState to .logic.ts considered — out-of-layer for align-design (red/green-frontend already [x] with sortUserRows as the logic boundary; pure transform is already extracted+tested); deferred. lint(oxlint/eslint/prettier/vue-tsc) exit 0; IDE inspections clean on both files; all <200.)
- [x] green-playwright (removed test.fail() + 6-line RED comment from users-grid.spec.ts Scn 3.2; frontend-only test — /api/auth/me + /api/admin/users mocked via page.route (CurrentUserBackendStatements + AdminUsersBackendStatements), no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` from frontend/; full users-grid file 4/4 green (Scn 3.2 1 passed (2.0s); no regression to 2.1/2.2/3.1). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [x] demo (recorded Scn 3.2 column-header sort E2E in headless slowMo=2000 + video on; 1 passed (11.4s); recording → frontend/test-results/demo-users-grid-sort.webm (gitignored). FE-only (page.route mocks, no backend). playwright.config.ts demo edits reverted, working tree clean.)

### Scenario 3.3: Timestamps show relative time with an absolute-on-hover tooltip
- [x] red-playwright (users-grid.spec.ts Scn 3.3 test block + new users-grid-time.statements.ts (hoverOverCreatedCell + per-part tooltip asserts; contract = `users-created-tooltip` container + `tooltip-date`/`tooltip-time`/`tooltip-tz-label`/`tooltip-tz-id` children) + new users-grid-time.fixture.ts (FIXED_NOW_INSTANT `2026-06-29T12:34:56.789Z` non-round; EXPECTED_RELATIVE_LABEL `17 days ago` for David Lee whose createdAt is 17.14d before FIXED_NOW — robust to floor-vs-round; RELATIVE_TIME_ROW_INDEX DERIVED by `login==='d.lee'`, not a magic number); current-user-backend.statements.ts gains optional timeZone (default `Europe/Berlin`, Scn 4.1 app default "Central Europe") on the mocked /api/auth/me — viewer-timezone plumbing the green component reads; zod schema non-strict tolerates the extra key (note for green-frontend-api to type+validate `timeZone`). DETERMINISM: `page.clock.setFixedTime(FIXED_NOW_INSTANT)` fakes Date.now/new Date only (leaves timers real — safest for a Vue app with HMR/reactivity), run as the FIRST action before navigation, so the relative label is deterministic regardless of CI wall-clock; exact page.clock API verified via Context7. RED: Created cell renders raw ISO `2026-06-12T09:14:37.482Z` ≠ `17 days ago`; no tooltip element exists → getByTestId 0 elems. test.fail() locked; bounded assertGridIsVisible() (5s) fronts the deep assert so a 30s whole-test timeout isn't absorbed (Scn 3.1 quirk). Prediction all-YES (Type=expect toHaveText Error / Message raw-ISO-vs-label / Status 1 passed expected-fail). 1 passed (9.9s); 0 failed/0 skipped. test-review tightened RELATIVE_TIME_ROW_INDEX from magic-3 to findIndex(login) + corrected a misleading fixture comment; 2 `toContainText` (date/time) kept deliberately loose — the absolute DISPLAY format is green/align-design's call (ISO vs humanized), and the time fragment still discriminates viewer-tz CEST `11:14` from raw UTC `09:14`. refactor extracted the Scn 3.3 constants to users-grid-time.fixture.ts so admin-users-fixture.ts dropped 200→181 (headroom regained); no asserted value / `test.fail()` marker touched. lint(oxlint/eslint/prettier/vue-tsc) green; IDE `get_file_problems` errors:[]; all files <200. Story scenario → no issue tag.)
- [x] red-frontend (2 new pure logic functions for the Created cell: `toRelativeTimeLabel(iso, now)` + `toAbsoluteTooltipParts(iso, tz)` → new `AbsoluteTimeParts{date,time,tzLabel,ianaZone}` type. RED pass-through stubs (returns raw ISO / empty parts). New `users-grid.timestamps.logic.test.ts`. Committed cab67f3 with a thin 6-case set (only 17.14d→"17 days ago" + just-now + 4 tooltip parts on one row); user flagged it as under-covering the cheapest test layer — expanded to the FULL contract corner-case matrix BEFORE green. CONTRACT (user-approved A/B1/C): fixed relative-time scale with FLOOR rounding + singular-at-1 — just now <60s | minutes <60m | hours <24h | days <7d | weeks <30d | months <365d | years ≥365d (30d/365d divisors). Expanded test = 19 it.fails: 13-row it.fails.each covering every bucket + both singular/plural sides + exact transition edges (60s→1min, 60m→1hr, 24h→1day, 7d→1week) with pre-computed anchor ISOs (NOW=FIXED_NOW_INSTANT, no arithmetic) + 6 tooltip cases (summer CEST date/time/tzLabel/ianaZone, winter→CET DST-aware, Asia/Tokyo midnight date-rollover). CONTRACT FINDINGS (empirically probed via node Intl, Node25/ICU78): (a) David Lee 17.14d → "2 weeks ago" under B1 (was "17 days ago") → updated EXPECTED_RELATIVE_LABEL in the E2E fixture; (b) CEST/CET emitted ONLY under en-GB locale (en-US gives "GMT+2") → green must format en-GB, pinned in spec; (c) JST unreachable (Intl→GMT+9) so rollover edge asserts date/time shift not a letter tz. Empty/invalid timeZone NOT tested — unreachable (profile enforces a valid IANA; invalid-zone is Scn 5.5 web-slice 422). RED: stubs return wrong values → AssertionError; 19 expected-fail/0 fail; full FE suite 73 passed/19 expected-fail/0. ALSO updated to the new contract (same commit): mockups (desktop 01/02 + mobile day-labels → week-labels + scale comment), 02_UI_Tests.md Scn 3.3 (full bucket table + en-GB/rollover notes), E2E fixture David-Lee label+comment. test-review CLEAN (0 edits — strict toBe, no arithmetic, singular/plural+edge coverage confirmed, winter/Tokyo genuinely discriminate, lock-step verified). refactor no-op (test 75 lines, idiomatic) + earlier Extract Method on buildUserRows (toUserRow helper, pre-existing Scn 2.1 code) in cab67f3. lint(oxlint/eslint/prettier/type-check) green; IDE errors:[]; all <200. Story scenario → no issue tag.)
- [x] green-frontend (both pass-through stubs implemented in users-grid.logic.ts: toRelativeTimeLabel = B1 fixed-scale FLOOR cascade (just now <60s → minute/hour/day/week/month/year, 7/30/365 divisors) with a `timeAgo(count,unit)` helper doing singular-at-1 pluralization; toAbsoluteTooltipParts = Intl.DateTimeFormat('en-GB',{timeZone,…,hour12:false,timeZoneName:'short'}).formatToParts → named-part picking via `partValue` helper (no index/regex nav), date=yyyy-MM-dd / time=HH:mm in viewer TZ / tzLabel=DST-aware CEST/CET (en-GB locale is what emits the abbreviation; en-US gives GMT+2) / ianaZone=passed-through. 19 it.fails/it.fails.each flipped to it/it.each, no assertion/fixture/expected touched, stale RED comments stripped. Target 19/19; full FE suite 92/92 no regressions. /refactor CLEAN no-op (timeAgo + partValue already extract the count-format + accessor-chain smells; bucket cascade deliberately kept sequential — a lookup table needs a just-now guard + magic second-arithmetic + non-null find assertion, reads worse). logic 134 lines (<200). lint(oxlint/eslint/prettier/vue-tsc) exit 0; IDE get_file_problems errors:[] on both files. Story scenario → no issue tag.)
- [x] red-frontend-api (current-user contract gains `timeZone`: the grid tooltip renders createdAt/updatedAt in the VIEWER's profile timezone, which flows from GET /api/auth/me → AuthenticatedUser must carry timeZone (auth.store holds it, align-design reads it for toAbsoluteTooltipParts). RED in current-user.api.test.ts: converted the authenticated-200 test to `it.fails`, stub body + expected gain `timeZone:'Europe/Berlin'` (lock-step with VIEWER_TIME_ZONE_ID in the E2E users-grid-time.fixture). RED reason = currentUserResponseSchema is a z.object that STRIPS the unknown `timeZone` key → parsed user drops it → whole-object toEqual fails on the missing timeZone; pinned by the literal value (it.fails can't pin a type). ZERO production files changed (schema+type addition is the GREEN seam; expected literal left un-annotated because timeZone isn't in AuthenticatedUser yet). Prediction all-YES (AssertionError/toEqual, user.timeZone undefined vs 'Europe/Berlin', it.fails→1 pass+1 expected-fail). test-review tightened the split toMatchObject+toHaveProperty into one strict whole-object toEqual. refactor CLEAN no-op (77 lines, idiomatic, sibling-consistent). lint(oxlint/eslint/prettier/vue-tsc) green; IDE get_file_problems errors:[]. Story scenario → no issue tag. BACKEND GAP (out of scope here, needs follow-up): backend CurrentUserResponse does NOT yet emit `timeZone` — the GREEN'd FE schema will parse it as missing against the live /api/auth/me until a backend story/task adds the field.)
- [x] green-frontend-api (minimal seam flips the it.fails → it: currentUserResponseSchema gains `timeZone: z.string()` (required boundary validation — the viewer's profile timezone is always present) + AuthenticatedUser gains `readonly timeZone: string`. fetchCurrentUser/auth.store need NO change — the parsed body flows through `{authenticated,user}` and the store assigns result.user, so currentUser now carries timeZone for align-design. The new required field rippled to 4 AuthenticatedUser/`/me`-stub construction sites (dashboard-user.logic.test, fetch.api.test, auth.store.test JOHN_DOE + /me body, home.smoke.test /me body) — each fixed with a realistic `'Europe/Berlin'` (NOT weakened to optional; strict schema would otherwise throw in loadMe). Target current-user.api.test 2/0/0; full FE unit suite 92/0/0 (20 files); lint(oxlint/eslint/prettier/vue-tsc) green; IDE get_file_problems errors:[] on schema/types/test. /refactor re-annotated the now-complete expected literal as `const expected: CurrentUserResult` (type carries timeZone now); rejected a cross-file shared 'Europe/Berlin' constant (over-coupling — each test file owns its local fixture). BACKEND GAP still open (out of scope): /api/auth/me does not yet emit timeZone — FE schema will see it missing against the live backend until a backend story/task adds it.)
- [x] align-design (Created + Updated cells now render relative time + an absolute-on-hover tooltip per mockup 01-users-grid.html `.ts-rel`. New presentational `TimeCell.vue` (56L): `.ts-rel` span = `toRelativeTimeLabel(iso, now)` (logic, green from Scn 3.3); on `@mouseenter` a **Teleport-to-body** `:data-testid` tooltip (`users-created-tooltip`/`users-updated-tooltip`) renders `toAbsoluteTooltipParts(iso, timeZone)` split into `tooltip-date`/`tooltip-time`/`tooltip-tz-label`/`tooltip-tz-id` spans (+ static `·` aria-hidden glyph between zone parts). Teleport+`v-if="hovered"`+`position:fixed` from the anchor's getBoundingClientRect = (a) escapes the `.table-card` `overflow:hidden` clip and (b) keeps exactly ONE tooltip in the DOM at a time so the page-level `getByTestId` locator stays single-element (no Playwright strict-mode violation across 8 rows). UsersGrid: `now = new Date()` captured once at setup + new `viewerTimeZone` prop, two `<TimeCell>` (created/updated) replace the raw-ISO `{{ row.createdAt/updatedAt }}` cells. UsersPage (orchestrator): reads `useAuthStore().currentUser?.timeZone ?? 'UTC'` (computed `viewerTimeZone`) and threads it down — viewer's profile zone drives the absolute tooltip. style.css: `.ts-rel` (cursor-help, dotted muted underline — mockup match) + `.ts-tooltip` (fixed/z-50 dark sidebar popover) + `.ts-tooltip-time`/`.ts-tooltip-zone` (opaque shadow extracted per CSS rule). design-review PASS (no mockup placeholder literals — grep for Europe/Berlin/CEST/UTC+/2026-/"week ago"/Doe/Connor across components = 0; every value derives from API rows + auth-store TZ). refactor CLEAN no-op (no Fowler smells; TimeCell 56 / UsersGrid 153 / UsersPage 47 / style.css 181, all <200; two `<TimeCell>` kept explicit not looped — clearer; TimeCell stays feature-local, B12 2-usage promotion trigger unmet). align-design verify: Playwright users-grid 2.1/2.2/3.1/3.2 ✓ no regression, Scn 3.3 = "expected-fail-but-passed" (impl works — relative label + all 4 tooltip parts in viewer TZ correct; `test.fail()` flips in green-playwright). coverage: users-grid.logic.ts 100% stmt/lines (lone uncovered branch L114 `?? ''` = pre-existing green-frontend defensive guard, unreachable — Intl always emits requested parts; not this step's code); TimeCell/UsersGrid/UsersPage presentational → E2E-covered, no new reachable unit gap. build(vue-tsc)+lint(oxlint/eslint/prettier/type-check)+92/92 vitest green; all files <200. Story scenario → no issue tag.)
- [x] green-playwright (removed test.fail() + 4-line RED comment from users-grid.spec.ts Scn 3.3; frontend-only test — /api/auth/me + /api/admin/users mocked via page.route (CurrentUserBackendStatements + AdminUsersBackendStatements), clock frozen at FIXED_NOW_INSTANT via page.clock.setFixedTime, no real backend; Playwright webServer auto-started Vite. Run via `--project=chromium` from frontend/; full users-grid file 5/5 green (Scn 3.3 1 passed (1.9s); no regression to 2.1/2.2/3.1/3.2). Remove-marker-only, no production/Statements changes. prettier --check clean on the spec.)
- [~] demo

### Backend Foundation (deferred): GET /api/auth/me returns the viewer's timeZone (L1 acceptance)
> WHY DEFERRED & PLACED HERE: endpoints.md declares `GET /api/auth/me` MODIFIED → response carries
> `timeZone` (viewer's profile zone), but no Story 4 backend scenario ever drove it — the create/grid
> scenarios don't touch /me, so the foundation line stayed declarative and CurrentUserResponse was never
> extended. Scn 3.3 green-frontend-api made the FE schema REQUIRE timeZone from /me, so against the LIVE
> backend `currentUserResponseSchema.parse` now THROWS (auth.store.loadMe fails → dashboard bootstrap
> breaks); FE-mocked E2E hides it, but the real app + full-stack journey break. USER DECISION (2026-06-30):
> close the gap AFTER Scn 3.3 (align-design+demo), before Scn 4.1 — hence this block's position so
> /continue picks it up next once Scn 3.3 demo is [x].
> SCOPE (verified isolated): User domain already has timeZone (Scn 3.1) and AuthenticationService already
> returns the User → ONLY the REST response DTO mapping is missing. activate returns ActivationTokenResponse
> (login+email), NOT CurrentUserResponse, so there is NO web-slice collateral; only CurrentUserInfoIntegrationTest
> (the /me whole-response acceptance test) asserts the body. Admin seed zone = `UTC` (user.csv).
- [ ] red-acceptance (extend CurrentUserInfoIntegrationTest.should_returnOwnUserInfo_when_authenticated: add `"timeZone": "UTC"` to the whole-response expected JSON → @ExpectedToFail; today CurrentUserResponse has no timeZone field so the response omits it. Predict 200 + JSON-missing-field assertion failure.)
- [S] design (trivial REST DTO field; serialized as the IANA id string via ZoneId.getId() per the create-user-timezone ADR; no new ADR)
- [S] red-usecase (no usecase logic — AuthenticationService already loads & returns the User carrying timeZone)
- [S] green-usecase
- [S] red-domain (User.timeZone already exists and is covered from Scn 3.1)
- [S] green-domain
- [ ] adapters-discovery (REST response DTO mapping only: CurrentUserResponse.from is simple delegation, no validation/error logic → [S] adapter test; the field + mapping are created in green-acceptance under the simple-delegation plumbing exception. No db/usecase ports change. Verified no web-slice collateral — activate uses ActivationTokenResponse.)
- [ ] green-acceptance (add `String timeZone` to the CurrentUserResponse record + map `user.getTimeZone().getId()` in from(); remove @ExpectedToFail. Simple-delegation plumbing exception. Run CurrentUserInfoIntegrationTest + full suite.)

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
