# Task 223: DB baseline cleanup as a JUnit extension

Type: refactoring
Issue: #223

## Problem

`AbstractApplicationIntegrationTest` has two `@BeforeEach` methods (clock reset + `iam_user`
baseline reset added in Story 4 Scenario 1.1). SonarLint `java:S8745` flags "only one method in a
class should be annotated `@BeforeEach`". The reset also injects a `JdbcTemplate` field into the
shared context base, mixing the DB-cleanup responsibility into it.

## Solution

Extract the baseline reset into a dedicated `BeforeEachCallback` JUnit extension that resolves the
datasource from the Spring `ApplicationContext` (`SpringExtension.getApplicationContext(ctx)`) and
runs the delete via the Spring 6 `JdbcClient` (`jdbcClient.sql("DELETE FROM …").update()`) instead
of `JdbcTemplate`. Register it with `@ExtendWith` on the full-context meta-annotation
`@ApplicationIntegrationTest` (the layer that commits users and asserts read-all) — not `@DbTest`
(rolled-back `@DataJpaTest` slices). Remove the second `@BeforeEach` and the injected field.

Covers Story 4 review Q5 (JdbcClient) + Q6 (S8745). Backlog ref: Story 4 improvements.md I2.

## Key Files

- `src/test/java/by/iivanov/rpm/testing/AbstractApplicationIntegrationTest.java`
- `src/test/java/by/iivanov/rpm/testing/ApplicationIntegrationTest.java`
- new: `src/test/java/by/iivanov/rpm/testing/*CleanupExtension.java`
