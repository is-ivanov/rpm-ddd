# Task 224: Serialize DB tests with @ResourceLock instead of SAME_THREAD

Type: refactoring
Issue: #224

## Problem

Blanket `@Execution(ExecutionMode.SAME_THREAD)` guards a shared resource in two places, but it forces
the node into the engine's main thread — so ALL such classes serialize against EACH OTHER (not just
within their own group), and any non-shared work they contain loses parallelism:

1. `@ApplicationIntegrationTest` (full-context) — only shared resource is the Testcontainers Postgres.
2. Web-slice tests (`@WebTest` classes `AuthResourceTest`, `UserResourceTest`) carry per-class
   `@Execution(SAME_THREAD)` — their only shared resource is the auto-mocked controller-dependency
   beans in the cached `@WebMvcTest` context (concurrent stubbing of the same mock races).

The two groups share no state with each other, yet SAME_THREAD lumps them (plus `@DbTest`) into one
serial lane.

## Solution

Replace the blanket `SAME_THREAD` with a per-shared-resource `@ResourceLock(..., mode = READ_WRITE)`
so each group serializes only against its own resource and the three lanes (DB / web-slice / pure
unit) run in parallel:

1. **DB lane** — annotate `@ApplicationIntegrationTest` and `@DbTest` with `@ResourceLock("DB")`;
   ensure both markers share the lock key (the `@DataJpaTest` SQLi test inherits `@DbTest`, so it
   joins the same lane if it shares the container).
2. **Web-slice lane** — annotate `@WebTest` with `@ResourceLock("WEB_SLICE_MOCKS")` and drop the
   per-class `@Execution(SAME_THREAD)` from `AuthResourceTest` and `UserResourceTest`.

Remove all blanket `SAME_THREAD`. **Validate carefully** — a flaky parallel test is costly:
- a class-level `@ResourceLock` makes whole *classes* mutually exclusive, but methods within one
  class still run concurrently under `mode.default=concurrent` and share the same mocks/DB. Confirm
  no two same-lane methods (across all classes in the lane) run at once — keep method-level
  serialization if the class-level lock does not provide it.
- confirm removing `SAME_THREAD` exposes no ordering assumptions, and that the lanes actually
  parallelize (timing) rather than silently collapsing back to one thread.

Covers Story 4 review Q7. Backlog ref: Story 4 improvements.md I3.

## Key Files

- `src/test/java/by/iivanov/rpm/testing/ApplicationIntegrationTest.java`
- `src/test/java/by/iivanov/rpm/testing/DbTest.java`
- `src/test/java/by/iivanov/rpm/testing/WebTest.java`
- `src/test/java/by/iivanov/rpm/iam/user/infrastructure/web/AuthResourceTest.java`
- `src/test/java/by/iivanov/rpm/iam/user/infrastructure/web/UserResourceTest.java`
