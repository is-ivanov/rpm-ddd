# Scenario 5.3 — Passwords are stored hashed

## design (2026-06-14)

**Surprise:** Production stores `{bcrypt}$2a$…`, not bare `$2a$…`, because the wired `DelegatingPasswordEncoder` prepends the `{bcrypt}` algorithm id — so test-spec §5.3's "starts with $2a$" is inaccurate.
**Why:** The encoder is `PasswordEncoderFactories.createDelegatingPasswordEncoder()`, which prefixes the id for multi-algorithm support.
**Impact:** Hash-format assertions must use `startsWith("{bcrypt}$2a$")`; the pre-existing `PasswordPolicyTest` used `NoOpPasswordEncoder` and never covered the real format or plaintext-absence.
