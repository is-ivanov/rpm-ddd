# Scenario 5.5 — Expired JWT activation token is rejected

## green-adapter rest (2026-06-15)

**Quirk:** `error.handling.messages` overrides are keyed globally by exception class, not per-endpoint — one entry sets the Problem Detail `detail` for every request path that surfaces that exception type.
**Where:** `src/main/resources/application.yml` (`error.handling.messages`); wimdeblauwe error-handling starter.
**Implication:** Two endpoints throwing the same exception type cannot show different `detail` texts via config alone; to diverge, wrap one path in a distinct domain exception and map that type instead.

**Decision:** GET `/api/auth/validate` and POST `/api/auth/activate` share one expired-token detail "Activation token has expired"; Scenario 2.2's GET test was updated (its "Token expired" was a raw-jjwt mock artifact, not a spec contract — §2.2 only requires "an error indicating the token issue").
**Why:** Same activation token and same expiry condition → one clean user-facing message, and it keeps the design config-only (zero Java production code) despite the global-keying constraint above.
**Where applied:** `application.yml` `error.handling.messages`; `AuthResourceTest.ValidateActivationTokenTest`.
