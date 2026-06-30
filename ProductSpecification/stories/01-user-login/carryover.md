# Story 1 — Carryover

Enduring codebase quirks and decisions promoted from completed scenarios. Read on resume; verify against current code before relying on them.

## Quirk: error.handling.messages is global per exception type
**Quirk:** `error.handling.messages` overrides are keyed globally by exception class, not per-endpoint — one entry sets the Problem Detail `detail` for every request path that surfaces that exception type.
**Where:** `src/main/resources/application.yml` (`error.handling.messages`); wimdeblauwe error-handling starter.
**Implication:** Two endpoints throwing the same exception type cannot show different `detail` texts via config alone; to diverge, wrap one path in a distinct domain exception and map that type instead.
**From:** scenario 5.5 (5-5-expired-jwt)

## Decision: one expired-token message across validate + activate
**Decision:** GET `/api/auth/validate` and POST `/api/auth/activate` share one expired-token detail "Activation token has expired" (Scenario 2.2's GET test's "Token expired" was a raw-jjwt mock artifact, not a spec contract).
**Why:** Same activation token and same expiry condition → one clean user-facing message, and it keeps the mapping config-only.
**Where applied:** `application.yml` `error.handling.messages`; `AuthResourceTest.ValidateActivationTokenTest`.
**From:** scenario 5.5 (5-5-expired-jwt)

## Quirk: jjwt exceptions must each be mapped individually (no super-class matching)
**Quirk:** The error-handling starter does not match exception super-class hierarchy, so each jjwt exception type needs its own `http-statuses` entry; `SignatureException` fell to 500 until mapped → 422.
**Where:** `application.yml` `error.handling.http-statuses`; complements the global-per-type `error.handling.messages` quirk above.
**From:** scenario 5.4 (5-4-tampered-jwt)

## Quirk: AuthResourceTest shares one activationService mock across nested tests
**Quirk:** `AuthResourceTest` shares one `activationService` mock across its nested classes, so new stubbing needs `@Execution(SAME_THREAD)` to avoid racing sibling tests.
**Where:** `AuthResourceTest`.
**From:** scenario 5.4 (5-4-tampered-jwt)

## Quirk: no domain role concept — CurrentUserResponse.roles is hardcoded empty
**Quirk:** There is no role concept yet; `CurrentUserResponse.roles` is hardcoded `List.of()`, so role state is not HTTP-observable.
**Where:** `CurrentUserResponse` / `/api/auth/me`.
**From:** scenario 5.7 (5-7-mass-assignment)

## Quirk: one DOM element cannot carry two data-testids
**Quirk:** One element can't hold two `data-testid`s; when a redesign changes a testid, migrate the dependent scenario's Statements rather than double-tagging.
**Where:** `activation-page.statements.ts`.
**From:** scenario 4.2 (4-2-password-strength)

## Quirk: Playwright not.toBeEmpty() never passes on SVG-only icon wrappers
**Quirk:** `not.toBeEmpty()` asserts on element text, so on an SVG-only icon wrapper it can never pass — assert `svg > *` is attached instead.
**Where:** activation/login icon Statements (`assertScreenIconIsVisible`).
**From:** scenario 5.1 frontend (5-1-activation-success)
