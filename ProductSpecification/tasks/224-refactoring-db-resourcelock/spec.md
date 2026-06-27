# Task 224: Serialize DB tests with @ResourceLock instead of SAME_THREAD

Type: refactoring
Issue: #224

## Problem

`@ApplicationIntegrationTest` forces `@Execution(ExecutionMode.SAME_THREAD)`, serializing all
full-context tests even though the only shared resource is the Testcontainers Postgres. Unit/domain
tests in those classes lose parallelism they do not need.

## Solution

Annotate every DB-touching meta-annotation (`@ApplicationIntegrationTest` and `@DbTest`) with
`@ResourceLock("DB", mode = READ_WRITE)` so only DB tests serialize on the shared database while
unit/domain tests keep running in parallel. Remove the blanket `SAME_THREAD`. Validate carefully —
a flaky parallel DB test is costly; ensure both markers share the lock key and that removing
`SAME_THREAD` does not expose ordering assumptions.

Covers Story 4 review Q7. Backlog ref: Story 4 improvements.md I3.

## Key Files

- `src/test/java/by/iivanov/rpm/testing/ApplicationIntegrationTest.java`
- `src/test/java/by/iivanov/rpm/testing/DbTest.java`
