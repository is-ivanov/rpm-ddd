# Task 178: Collapse iam.auth into iam.user (remove auth subpackage)

Type: refactoring
Issue: #178

## Problem

The `iam` context was split into `auth` and `user` subpackages along the wrong axis — by *layer*, not by capability. `iam.auth` holds only an infrastructure slice (`SecurityConfig`, `RpmUserDetails(Service)`, `ProblemDetailAccessDeniedHandler`, `AuthResource` + DTOs), while all authentication domain and application logic lives in `iam.user`. Credentials (`Login`, `Password`, `LoginThrottle`) are embedded value objects of the `User` aggregate, and authentication behavior lives on `User` itself — so `auth` is a hollow subdomain whose name claims a boundary that does not exist.

See `decisions/iam-auth-user-packaging-decision.md` for the full rationale.

## Solution

Pure package move — no behavior change, `AuthResource` URL paths unchanged:

- `auth.infrastructure.{SecurityConfig, RpmUserDetails, RpmUserDetailsService, ProblemDetailAccessDeniedHandler}` → `user.infrastructure.security`
- `auth.infrastructure.web.{AuthResource, LoginRequest, ActivateAccountRequest, CurrentUserResponse, ActivationTokenResponse}` → `user.infrastructure.web`
- Tests `iam.auth.*` (integration, fixtures, infrastructure) → corresponding `iam.user.*` packages
- Test resources `__files/iam/auth/web/*` → `__files/iam/user/web/`
- Delete `auth` `package-info.java` files; remove the empty `auth` package

A real authorization context (roles/permissions) is deferred — it will be its own subdomain referencing `User` by `UserId`, never by sharing value objects.

## Affected Layers

- infrastructure (security adapters, REST web resources) — production
- tests (integration, web slice, fixtures, `__files` resources)

## Key Files

- `src/main/java/by/iivanov/rpm/iam/auth/**` (entire package — moved/deleted)
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/security/**` (destination for security adapters)
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/web/**` (destination for auth web)
- `src/test/java/by/iivanov/rpm/iam/auth/**` (entire package — moved/deleted)
- `src/test/resources/__files/iam/auth/web/**` → `__files/iam/user/web/`
- `src/test/java/by/iivanov/rpm/ArchitectureTest.java` (Modulith/onion verification gate)
