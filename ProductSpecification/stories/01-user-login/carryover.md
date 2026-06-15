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
