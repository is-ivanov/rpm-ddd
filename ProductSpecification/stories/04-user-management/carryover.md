# Story 4 — Carryover (enduring quirks & invariants)

## Quirk: read-model lazy associations break outside a transaction
**Quirk:** A read-model's self-referencing `@ManyToOne` consumed during adapter mapping must be eagerly fetched (`@EntityGraph(attributePaths = {...})` on the repository finder); a `LAZY` association resolves under `@DataJpaTest`'s open transaction but throws `LazyInitializationException` (500) in the non-transactional running read path.
**Where:** `iam.user.infrastructure.persistence.SpringDataUserSummaryRepository` / `JpaUserSummaryQuery`.
**Implication:** never rely on lazy resolution during mapping in a non-transactional application service; fetch-join in the one query.
**From:** scenario 1.1 (1.1-list-users)

## Quirk: seed timestamps are TZ-naive; the seed load pins the session to UTC
**Quirk:** `db/data/user.csv` timestamps are offset-less (`timestamptz` column); pgjdbc sets the session zone to the JVM default, so without a pin the seed stores at the host offset (off the UTC fixtures). Liquibase only recognizes the offset-less `yyyy-MM-dd'T'HH:mm:ss` form, so a `Z` literal cannot be used.
**Where:** `src/test/resources/db/changelog/db.changelog-test.xml` (`test-data-user` runs `SET TIME ZONE 'UTC'` before `loadUpdateData`).
**Implication:** keep seed timestamps offset-less and rely on the session pin; expected fixtures are UTC.
**From:** scenario 1.1 (1.1-list-users)

## Quirk: full-context tests share one committed Postgres — baseline reset per test
**Quirk:** Full-context integration tests commit their data (no rollback) into one shared container, so `iam_user` accumulates users across the suite; `AbstractApplicationIntegrationTest` now deletes non-seed (`id not like '019b76da%'`), non-system (`<> 0000…`) rows `@BeforeEach` to restore the seeded baseline.
**Where:** `by.iivanov.rpm.testing.AbstractApplicationIntegrationTest`.
**Implication:** any read-all or count assertion over a shared aggregate depends on this reset; seed ids must keep the `019b76da` prefix to survive it.
**From:** scenario 1.1 (1.1-list-users)

## Quirk: custom ApiExceptionHandler is auto-discovered into the web-slice context via WebTest
**Quirk:** A wim-deblauwe `ApiExceptionHandler` declared as a plain `@Component` is NOT loaded by a bare `@WebMvcTest` (only web stereotypes are scanned), so a web-slice test of the mapping would get the starter's fallback 500. Resolved centrally: `WebTest`'s `@ComponentScan` has an `ASSIGNABLE_TYPE` includeFilter for `ApiExceptionHandler`, so EVERY handler is auto-discovered into the single shared web-slice context.
**Where:** `by.iivanov.rpm.testing.WebTest` (`@ComponentScan` includeFilters), `iam.user.infrastructure.web.LoginAlreadyExistsExceptionHandler`.
**Implication:** a new custom `ApiExceptionHandler` (e.g. timezone validation in 5.5, a future EmailAlreadyExists handler) needs NO per-test `@Import` — just declare it as a bean; it loads automatically and all web-slice tests keep one shared context. Never add `@Import(SomeHandler.class)` to a single web-slice test (it forks a second context).
**From:** scenario 2.1 (2.1-duplicate-login-422)

## Decision: domain exception → field-level 422 via a custom ApiExceptionHandler
**Decision:** To surface a domain exception as a field-level 422 with the bean-validation `fieldErrors` shape, implement an `ApiExceptionHandler` building `ApiErrorResponse(status, "VALIDATION_FAILED", "...Error count: N")` + `addFieldError(new ApiFieldError(code, property, message, rejectedValue, path))`; config-based `http-statuses` cannot emit a `fieldErrors` array.
**Where applied:** `iam.user.infrastructure.web` exception handlers — reusable for 5.5 (timezone) and any uniqueness/duplicate mapping.
**From:** scenario 2.1 (2.1-duplicate-login-422)

## Decision: admin pages navigate via a nested-route layout
**Decision:** `DashboardShell` hosts a nested `<RouterView>`; `/` keeps the top-bar+sidebar chrome with `''`→`DashboardHome` and `/users`→`UsersPage` as child routes, and sidebar items are `<RouterLink>`s with route-name active state (not an `activeSection` view-state ref).
**Where applied:** `frontend/src/router/index.ts` (nested children), `DashboardShell.vue`, `DashboardHome.vue`; new Admin Center pages slot in as `/` children, and component tests that need the shell must mount `App`+router, not the page component directly.
**From:** scenario 1.2 (1.2-users-navigation)

## Quirk: test.fail() absorbs assertions but not a whole-test timeout
**Quirk:** Playwright's `test.fail()` keeps a RED spec as "expected-fail" only when the failure is a thrown assertion/error; a 30s whole-test timeout (e.g. a `fill()` on a not-yet-existing locator) is NOT absorbed and fails the build red.
**Where:** `frontend/acceptance/tests/frontend/users/users-grid.spec.ts` and any `test.fail()`-locked RED spec.
**Implication:** front every `test.fail()` RED spec with a bounded visibility assertion (`assert*IsVisible()`, 5s `toBeVisible`) so the RED is a fast assertion the marker can absorb.
**From:** scenario 3.1 frontend (3.1-column-filter)

## Decision: the frontend owns the status lifecycle order & labels
**Decision:** The grid sorts status by an explicit `STATUS_LIFECYCLE_RANK` map on the display label with a `Number.MAX_SAFE_INTEGER` fallback for unmapped codes; the lifecycle order (a `UserStatus`-enum domain fact) is duplicated on the FE because the grid sorts client-side and the wire sends only the bare code.
**Where applied:** `frontend/src/features/users/logic/users-grid.logic.ts` (`statusRank`/`STATUS_LIFECYCLE_RANK`); the MAX_SAFE_INTEGER fallback prevents an unknown BE code from `NaN`-breaking the sort; single-sourcing it from the BE is deferred to improvement I10.
**From:** scenario 3.2 frontend (3.2-column-sort)

## Quirk: deterministic time in E2E via page.clock.setFixedTime
**Quirk:** Freeze time for a relative-time E2E with `page.clock.setFixedTime(FIXED_NOW)` as the FIRST action before navigation — it fakes `Date.now`/`new Date` only and leaves timers real (safe for a Vue app with HMR/reactivity), so the rendered relative label is CI-wall-clock-independent.
**Where:** `frontend/acceptance/tests/frontend/users/users-grid.spec.ts`.
**Implication:** any future time-dependent E2E should pin the clock this way (before navigation), not fake the whole clock.
**From:** scenario 3.3 frontend (3.3-relative-time-tooltip)

## Quirk: Intl emits CEST/CET only under the en-GB locale
**Quirk:** `Intl.DateTimeFormat` renders the DST-aware zone abbreviation (CEST/CET) only with the `en-GB` locale; `en-US` returns `GMT+2` and `Asia/Tokyo` never yields a letter abbreviation (`GMT+9`).
**Where:** `frontend/src/features/users/logic/users-grid.logic.ts` (`toAbsoluteTooltipParts` formats with `en-GB`).
**Implication:** keep the `en-GB` locale for tooltip zone formatting — changing it silently drops the abbreviation; assert non-abbreviating zones (Tokyo) by their date/time shift, not a tz letter.
**From:** scenario 3.3 frontend (3.3-relative-time-tooltip)

## Quirk: a required FE schema field obligates the live backend to emit it
**Quirk:** A `required` field in a zod boundary schema (e.g. `timeZone` on `currentUserResponseSchema`) makes `parse` THROW against a live backend that omits it — breaking the real app (`loadMe` → dashboard bootstrap) — while the FE-mocked Playwright suite stays green because the mock supplies the key.
**Where:** frontend `*.schema.ts` boundary schemas vs the live `/api/auth/me` (and any `endpoints.md`-declared response).
**Implication:** every required FE schema field needs a backing live-backend emit plus an acceptance assertion; a contract change declared in `endpoints.md` but never driven by a backend scenario rots silently until the real app / full-stack journey breaks.
**From:** scenario 3.3 frontend (3.3-relative-time-tooltip)

## Quirk: SpotBugs BC_UNCONFIRMED_CAST false positive on ApiExceptionHandler downcast
**Quirk:** SpotBugs flags `BC_UNCONFIRMED_CAST` on the contract-safe `(Subtype) throwable` cast in `ApiExceptionHandler.handle()` (canHandle guarantees the type, invisible cross-method); suppressed in `exclude-filter.xml` scoped to `*ExceptionHandler` in `infrastructure.web`.
**Where:** `code-quality-config/spotbugs/exclude-filter.xml`.
**Implication:** future ApiExceptionHandlers are already covered by the scoped filter — don't remove it, and don't rewrite as pattern-match-with-throw (that trades the FP for a permanently-uncovered branch).
**From:** scenario 2.1 (2.1-duplicate-login-422)

## Quirk: grid popovers must Teleport to body — table-card overflow clips absolute panels
**Quirk:** A dropdown/popover positioned `absolute` inside the users grid is clipped by `.table-card`'s `overflow-hidden` (kept for the card's rounded corners), so options/panels hanging below the filter row are cut off.
**Where:** `frontend/src/features/users/components/UsersStatusFilter.vue`, `TimeCell.vue`, `.table-card` in `frontend/src/styles/components.css`.
**Implication:** any grid popover (status dropdown, Created/Updated date-range calendar in Scn 3.7) must `Teleport to="body"` and position `fixed` from the trigger's `getBoundingClientRect()`; `absolute` positioning will be clipped.
**From:** scenario 3.6 (3.6-status-filter)
