# Red Phase — Per-Layer Test Notes (java-spring)

Layer-specific RED-phase detail loaded on demand by `red-agent`. Covers the acceptance, domain, and adapter layers, plus reuse checks before writing test infrastructure.

## Acceptance Layer: Running Backend Required

Acceptance tests need a live backend. Predictions must be about feature behavior (see `tdd-rules.md`).

0. Before running the test, ensure the shared test DB is up — see `.claude/tech/java-spring/infrastructure.md` → "Test Database" (idempotent; reused across runs, never stopped). This also applies when running a `db` adapter test (`@Tag("db")`).
1. Before running the test, ensure the backend is up (`/run-backend` or check health endpoint)
2. Predict the **actual application-level failure** — assertion error, wrong HTTP status, missing data, etc.
3. If the feature is already fully implemented and the test would pass, the prediction is "test passes" — skip straight to `green-acceptance` (mark red-usecase/green-usecase/adapters as `[S]` with reason)
4. If the test fails (new implementation needed), verify that `progress.md` has a `design` step after `red-acceptance`. If missing, add it — `design` is mandatory for every scenario requiring new implementation.
5. **One action, assert all consequences** (see `tdd-rules.md`). If the scenario adds a new observable consequence (e.g. an email, an event, persisted state) to an action ALREADY covered by an existing acceptance test, **extend that test** with new `then` assertions instead of creating a parallel acceptance class for the same action — Level 1 runs with the full context (DB, SMTP, …) already up, so a second full-context test for the same action is wasted wall-clock.

## Domain Layer: Optional, One Test Class per Domain Class

Domain tests are OPTIONAL — only create when the domain object has testable logic (validation, state transitions, computed fields, business rules). Skip when: the class is a plain data holder with no behavior.

ONE test means **one test class per domain class** (value object, entity, policy, enum). The class may contain multiple test methods (valid cases, invalid cases via parameterized tests).

- Mark **each** test method with the test disable marker (placed per-method, not on the class — see the tech binding's `red-phase-formats.md`)
- Predict failure for each test method
- Parameterized tests count as one test method for prediction purposes

## Adapter Layer: Multiple Test Methods

For adapter layers, follow the test pyramid from `TESTING.md`. Each level tests only what is NOT covered by the level above.

**rest adapter** (Level 2 — web slice):
- ONE test class per controller endpoint that has validation or error-handling logic.
- **Test**: validation errors (422), business-exception-to-status-code mapping (401, 404, 422).
- **Do NOT test**: happy path — covered by acceptance tests (Level 1).
- **Skip `[S]`**: when the endpoint is a simple delegation (controller → usecase → response) with no request validation and no error mapping.
- Happy-path response shape is verified by the acceptance test.

**db adapter** (infrastructure):
- Only for custom `@Query`, native SQL, Specifications, JOIN FETCH — see `.claude/tech/java-spring/templates/db/test-class.md`.
- Simple Spring Data derived queries (`findByXxx`, `existsByXxx`) → `[S]`.

**email / security / other adapters**:
- Only corner cases not covered by acceptance tests.

- Mark **each** test method with the test disable marker (placed per-method, not on the class — see the tech binding's `red-phase-formats.md`)
- Predict failure for **each** test method separately
- Every method must fail as predicted before its marker is added

## Reuse Checks (before writing)

Before creating new test infrastructure, search for existing pieces to reuse:

1. **Existing Statements assertions** — grep all Statements files (including the target file itself) for `assert*` methods covering the same domain concept or response fields (e.g., `status`, `priority`, `createdAt`). Check within the same Statements class first, then across classes. If an existing method already asserts the same fields, delegate to it and add only the scenario-specific assertions on top.
2. **Existing Fakes** — grep `fake/` for Fakes with the same structure. If structurally identical, extract a shared base class immediately rather than copy-pasting.
3. **Existing `@WebApi` classes (acceptance + rest layers)** — before creating a new `@WebApi`-annotated `AbstractApi` subclass, grep `fixtures/` packages for existing API classes targeting the same controller (search for the controller's `BASE_URI` or path prefix). If found → add the new endpoint method to the existing class. If not found → create a new `@WebApi` class. This applies to both acceptance tests (integration tests) and REST adapter tests (web slice tests).
