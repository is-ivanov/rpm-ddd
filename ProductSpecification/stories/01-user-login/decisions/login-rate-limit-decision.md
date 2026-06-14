# Decision: Login rate limiting as throttle state on the User aggregate

**Date**: 2026-06-14 **Scenarios**: 5.2 (Security)

Consecutive failed logins must temporarily lock an account across all backend instances, so the attempt counter has to be durable shared state — the only open question is where that state lives.

| Rejected | Why |
|----------|-----|
| Dedicated `LoginAttempt` aggregate + repository | Adds a second storage port queried in sequence inside `AuthenticationService` (tension with the single-rich-aggregate rule) plus a new table/migration/aggregate — for "throttle any login incl. non-existent" behaviour the scenario does not require. |
| Web-layer rate-limit filter (e.g. bucket4j) | Needs a distributed/DB-backed store to be correct across instances (awkward); bypasses the domain; cannot be driven by the test `Clock`; doesn't fit the pending `red-usecase`/`green-usecase` steps. |

**Chosen**: Embed a cohesive `LoginThrottle` value object on the existing `User` aggregate, persisted as columns on `iam_user`. `AuthenticationService.authenticate` reuses the already-loaded user (single `UserRepository` port) to check and record throttle state, gated by a typed `LoginRateLimitPolicy` config object and the existing `Clock` bean. The transient throttle is **distinct** from the permanent `UserStatus.LOCKED` admin lock.

## Model

- `LoginThrottle` — new domain value object embedded in `User`: `failedAttempts` (int) + `lockedUntil` (Instant, optional). Behaviour: `recordFailure(now, policy)`, `clear()`, `isLocked(now)`. Persisted as `failed_login_attempts` + `locked_until` columns on `iam_user` (column migration, no new table).
- `User` — delegates to its `LoginThrottle`: `isThrottled(now)`, `recordFailedLogin(now, policy)`, `clearFailedLogins()`.
- `LoginRateLimitPolicy` — typed `@ConfigurationProperties` object: `maxAttempts` (default 5) + `window` (Duration). One documented field per value.
- `AuthenticationService.authenticate` — gains `Clock` + `LoginRateLimitPolicy`. Flow: throttled? → throw; validate active; bad password → `recordFailedLogin` + `save` then throw `TooManyLoginAttemptsException` (if now locked) else `BadCredentialsException`; success → `clearFailedLogins` + `save`.
- `TooManyLoginAttemptsException` — new domain exception, message "Too many failed attempts". Mapped in `application.yml` → status `too-many-requests` (429) + code `too-many-login-attempts` (same config-driven mechanism as `UserAuthenticationException` → 401). RFC 9457 type `https://www.rpm-ddd.my/problem/too-many-login-attempts`, title "Too Many Requests".

## Edge Cases

| Case | Behavior |
|------|----------|
| 5th consecutive wrong password (4 already recorded) | Records the 5th failure → count reaches `maxAttempts` → sets `lockedUntil = now + window` → that same response is 429, not 401. |
| Correct password while locked within window | Start-of-method throttle check throws 429 before the password is verified — a valid password does not unlock early. |
| Window has elapsed | `isLocked(now)` false → attempt proceeds normally; a fresh failure starts counting again. |
| Successful login below threshold | `clearFailedLogins()` resets the counter so prior sporadic failures don't accumulate forever. |
| Non-existent login | Not throttled (no `User` row to carry state) — out of scope for this scenario; the existing "Account not activated" 401 path is unchanged. |
