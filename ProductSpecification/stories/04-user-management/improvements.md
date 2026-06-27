# Story 4 — Improvements Backlog

Enhancements and refinements found during Story 4. Architecture is deferred: finish the base story
in its current form, then revisit these. Move an item to `Done` with the resolving task/PR.

## Open

### I1 — Read-model projections live in the domain layer (Q1 + Q2)
**Observed:** `UserSummary` and `ActorName` sit in `iam.user.domain`, and the `UserSummaryQuery` port is a
domain interface. `ActorName` is explicitly validation-free ("lenient, no validation"), and `UserSummary`
is an anemic projection returned by a query — neither carries invariants or behaviour.
**Analysis:** these are CQRS read models, not write-side domain types. A domain value object validates; a
read projection does not. They fit `iam.user.application` (or `application.query`). `ActorName` is described
as a name triple reusable across contexts (users, patients, audit), so it is really a **shared-kernel** read
type → `shared`. Once relocated, `PersonNameResponse.from(nameTriple)` can move onto `PersonNameResponse`
(shared) and be reused everywhere (Q2) — today that move is blocked because `shared` must not depend on
`iam.user.domain` (ArchUnit/Modulith would reject it).
**Scope options:** (a) move `UserSummary` + `UserSummaryQuery` to `iam.user.application`, move the name triple
to `shared`, add `PersonNameResponse.from(...)`; (b) keep the projection in `iam.user` but relocate only the
name triple to `shared` for the `from` factory. Verify against `ArchitectureTest`.

### I2 — DB-baseline cleanup belongs in a JUnit extension, not a second @BeforeEach (Q5 + Q6) — Tracked as Task #223
**Observed:** `AbstractApplicationIntegrationTest` now has two `@BeforeEach` methods (clock reset + iam_user
baseline reset). SonarLint `java:S8745` flags "only one method should be `@BeforeEach`".
**Analysis:** extract the baseline reset into a dedicated `BeforeEachCallback` extension (single
responsibility) that resolves the datasource from the Spring `ApplicationContext`
(`SpringExtension.getApplicationContext(ctx)`) and runs the delete via the modern Spring 6 `JdbcClient`
(`jdbcClient.sql("DELETE FROM …").update()`) instead of `JdbcTemplate`. Register it via `@ExtendWith` on the
full-context meta-annotation `@ApplicationIntegrationTest` (the layer that commits users and asserts read-all)
— not `@DbTest`, which marks the `@DataJpaTest` slices. Removes the second `@BeforeEach` and the injected
field from the base.

### I3 — Replace blanket @Execution(SAME_THREAD) with @ResourceLock("DB") (Q7) — Tracked as Task #224
**Observed:** `@ApplicationIntegrationTest` forces `SAME_THREAD`, serializing all full-context tests even
though the only shared resource is the Testcontainers Postgres.
**Analysis:** annotate every DB-touching meta-annotation (`@ApplicationIntegrationTest` and `@DbTest`) with
`@ResourceLock("DB", mode = READ_WRITE)` so only DB tests serialize on the shared DB while pure unit/domain
tests keep running in parallel — better throughput and one explicit serialization mechanism. Validate
carefully: a flaky parallel DB test is costly; ensure both markers share the lock key.

### I4 — Set UTC in the production database/runtime, not only in tests (Q8) — Tracked as Task #225
**Observed:** the TZ saga (naive `timestamptz` seed read in the JVM zone) was fixed for tests with
`SET TIME ZONE 'UTC'` in `db.changelog-test.xml`. Production has no timezone pin
(`hibernate.jdbc.time_zone` is unset).
**Analysis:** a `SET TIME ZONE` in a migration does NOT fix runtime — each app connection resets to its own
session zone. The correct production fix is `spring.jpa.properties.hibernate.jdbc.time_zone=UTC` (Hibernate
normalizes all timestamp binding/reading to UTC regardless of JVM/DB zone), optionally plus `-Duser.timezone=UTC`
and container `TZ=UTC`. Note: Hibernate's setting does not cover Liquibase's own connection, so the test
seed-load pin may still be needed for the CSV load. Candidate for a standalone task (production-impacting).

### I5 — Static-analysis check for UPPER_CASE SQL/JPQL keywords (Q4) — Tracked as Task #226
**Observed:** the new rule "SQL/JPQL keywords are UPPER_CASE" (coding-rules.md → SQL & JPQL) is currently
convention-only.
**Analysis:** investigate enforcing it automatically. PMD/Checkstyle cannot parse SQL semantically inside Java
string literals; a Checkstyle `RegexpMultiline` over `@Query`/`.sql(...)`/string literals is possible but
false-positive-prone (matches keywords appearing as identifiers or in comments). An Error Prone custom check
could target `@Query` and `JdbcClient`/`JdbcTemplate` call sites more precisely. Candidate for a standalone
task to prototype feasibility and pick the mechanism.

### I6 — Timezone dropdown option source is unspecified (FE Scenario 4.1)
**Observed:** the create-user modal has a Timezone field (FE Scenario 4.1), but the spec only says it is
"pre-filled with the app default (Central Europe)" — it does NOT define where the **option list** for the
dropdown comes from. `endpoints.md` exposes `timeZone` only on `POST /api/admin/users` and `GET /api/auth/me`;
there is no zone-list endpoint. Under-specified by design (never built), so an improvement, not a bug.
**Analysis:** the dropdown must offer only zones the backend accepts, otherwise a user can pick a zone the
server 422-rejects. Our canonical valid-zone set is the JVM `ZoneId.getAvailableZoneIds()` — the same source
the create-user jakarta `@ValidTimeZone` constraint validates against (see `create-user-timezone-decision`).
Two options: (a) **`GET /api/timezones`** returning `ZoneId.getAvailableZoneIds()` (sorted) — FE fetches it;
zero FE/BE drift, one source of truth (recommended); (b) browser-native `Intl.supportedValuesOf('timeZone')` —
no new endpoint, but the browser's tz list may differ from the JVM's accepted set → drift/422 risk.
Pre-selection of the app default (`Europe/Berlin`) is independent of the option source.
**Scope:** small new read endpoint + FE client + dropdown wiring; decide at the Story Completion Gate or when
FE Scenario 4.1 is built (whichever comes first).

### I7 — Activation should update the audit fields shown in the grid (Extended E2, deferred at Backend Extended Gate)
**Observed:** Extended case E2 expects that when a PENDING user activates (sets a password), their grid row
shows status ACTIVE, `updatedAt` later than `createdAt`, and `updatedBy` resolving to the user themselves
(self-service activation). Today `User.activate(hashedPassword)` only flips status + password; `updatedAt`
and `updatedBy` are **`final`** fields set once in the constructor, so activation never touches the audit trail.
**Analysis:** never-built behaviour → improvement, not a bug. Implementing it requires making the audit pair
mutable on the aggregate and having `activate(...)` stamp `updatedAt = now(clock)` and `updatedBy = the
activating user` (a self-actor — the activation flow currently has no acting-principal plumbed through). This
touches the activation use case (clock + actor), the `User` aggregate (mutable audit), and is then observable
in the existing grid read-model. Sizable; needs a small design (where the self-actor comes from in the
activation request/principal).
**Scope:** L1 acceptance (activate → grid row ACTIVE with updatedAt>createdAt, updatedBy=self) + domain unit
for the audit stamp; activation use-case change + aggregate change. Decide whether to promote to a scenario or
raise as a standalone story/task. Deferred at the Backend Extended Gate (user decision).

### I8 — Lock the deterministic grid tiebreaker with an explicit test (Extended E3, deferred at Backend Extended Gate)
**Observed:** Extended case E3 wants the user list deterministically ordered when two users share the same
`createdAt` — tie broken by `userId` descending, stable across requests.
**Analysis:** the behaviour is **already implemented** — `JpaUserSummaryQuery.findAllForGrid()` sorts by
`createdAt DESC, id DESC`, so the tiebreaker exists. What is missing is an explicit test pinning the contract;
the dedicated `JpaUserSummaryQueryTest` was deleted in Scenario 1.1 as redundant with the L1 grid test, and
the L1 seed has distinct `createdAt` values, so the tie path is not exercised anywhere.
**Scope:** small `@DataJpaTest` (`red/green-adapter db`) — two users with identical `createdAt`, assert order
by `id` DESC and stability across repeated queries. Cheap regression lock. Deferred at the Backend Extended
Gate (user decision); promote when the read path is next touched, or leave relying on the implemented sort.
