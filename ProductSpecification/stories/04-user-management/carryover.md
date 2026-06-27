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
