# Decision: Collapse `iam.auth` into `iam.user`

**Date**: 2026-06-16 **Task**: #178 (iam packaging)

The `auth`/`user` split was carved by *layer*, not by capability: `auth` held only an infrastructure slice (SecurityConfig, UserDetails, AuthResource), while all authentication domain + application logic lived in `user`. Credentials are not a separate aggregate — `Login`, `Password`, `LoginThrottle` are embedded value objects of the `User` aggregate, and authentication behavior (`validateActiveForAuthentication`, `ensureNotThrottled`, `recordFailedLogin`, `activate`) lives on `User` itself.

| Rejected | Why |
|----------|-----|
| Keep `auth` as-is | Hollow package: web + security-config only, no domain/application; name claims a subdomain that doesn't exist. |
| Grow `auth` into a vertical slice (move `Login`/`Password`/tokens into it) | Would split the `User` aggregate across packages — root in `user`, its own VOs in `auth` — creating a `user.domain → auth.domain` dependency across a fake boundary. An aggregate's constituent VOs belong with the aggregate. |

**Chosen**: Move everything from `iam.auth` into `iam.user`; delete the `auth` package. `auth` is not a subdomain — credentials are part of the `User` aggregate, and the security wiring is infrastructure that serves it. A real authorization context (roles, permissions) is deferred: when it arrives it will be its own subdomain/context referencing `User` **by `UserId`**, never by sharing value objects.

## Model

- `auth.infrastructure.{SecurityConfig, RpmUserDetails, RpmUserDetailsService, ProblemDetailAccessDeniedHandler}` → `user.infrastructure.security`
- `auth.infrastructure.web.{AuthResource, LoginRequest, ActivateAccountRequest, CurrentUserResponse, ActivationTokenResponse}` → `user.infrastructure.web`
- Delete `auth/package-info.java`, `auth/infrastructure/package-info.java`, `auth/infrastructure/web/package-info.java`
- Tests: `iam.auth.*` (integration + fixtures + infrastructure) → corresponding `iam.user.*` packages
- Test resources: `__files/iam/auth/web/*` → `__files/iam/user/web/`
- `AuthResource` keeps its `@RequestMapping` URL paths unchanged — this is a package move, not an API change

## Edge Cases

| Case | Behavior |
|------|----------|
| Future authorization (roles/permissions) | New subdomain/bounded context; references `User` by `UserId`, holds no `User` value objects. |
| `SecurityConfig` is app-wide, not user-specific | Stays in `iam` (the only context that owns authentication today); lands in `user.infrastructure.security`. Revisit only if a second context needs the filter chain. |
