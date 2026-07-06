# Task 272: Extract error-code constants to a single source -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Introduce the error-code constant(s)
- [ ] refactor (add `ALREADY_EXISTS` — and optionally `VALIDATION_FAILED` — as a constant in a single source: extend `ErrorConstants` or add a sibling `ApiErrorCodes` in `shared.infrastructure.web.errors`; decide the home at implementation time)
- [ ] refactor (cleanup)

### Step 2: Reference the constant from all sites
- [ ] refactor (replace the literals in `EmailAlreadyExistsExceptionHandler`, `LoginAlreadyExistsExceptionHandler`, and the `UserResourceTest` assertions with the constant)
- [ ] refactor (cleanup)

## Verify
- [ ] green-acceptance (`./mvnw verify -B` green; RFC 9457 responses unchanged)
