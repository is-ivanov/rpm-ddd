# Task 272: Extract error-code constants to a single source

Type: refactoring
Issue: #272  <- the task number IS the issue number; refactoring records it for traceability and does NOT tag tests

## Problem

The API error `code` literals emitted from Java are hand-written string duplicates scattered across
the `iam.user.infrastructure.web` exception handlers:

- `"VALIDATION_FAILED"` (error-level `ApiErrorResponse` code) — duplicated in
  `EmailAlreadyExistsExceptionHandler:27` and `LoginAlreadyExistsExceptionHandler:27`. This is the
  error-handling starter's default code for bean-validation failures; the handlers mirror it so a
  duplicate-email/login response looks like a bean-validation error.
- `"ALREADY_EXISTS"` (field-level `ApiFieldError` code) — **our own** code, duplicated in both
  handlers (`:29`) **and** re-typed in `UserResourceTest` (assertions at `:50` and `:66`). A typo in
  one copy would silently drift the assertion from what production emits.

PMD `AvoidDuplicateLiterals` does **not** flag these — it is per-file, and each literal appears once
per file (the duplication is *across* files). So this is a pure DRY/design concern, not a
static-analysis finding. Discovered while discussing Task #244 batch 5b·5 (the PMD prod dups were the
within-file `"email"` / `"login"` / `"/api/auth/activate"`, a separate matter).

A constants home already exists:
`by.iivanov.rpm.shared.infrastructure.web.errors.ErrorConstants` (`public final`, private ctor, holds
`PROBLEM_BASE_URL` + `ACCESS_DENIED_TYPE`) — the same holder pattern used for `SpaRoutes`.

## Solution

Extract the duplicated error codes into a single source and reference it from every site.

- **`ALREADY_EXISTS`** — strongest case: our own code, 2 prod + 2 test sites. Extract to a constant,
  reference from both handlers and from `UserResourceTest`.
- **`VALIDATION_FAILED`** — a starter default we mirror; extract too so there is one place to update
  if the starter's default ever changes (decide at implementation time whether to include it).
- **Home** — decide at implementation time: extend the existing `ErrorConstants`, or add a sibling
  `ApiErrorCodes` in the same `errors` package (codes are a different axis than the URI type-prefixes
  `ErrorConstants` currently holds).

## Out of scope

- The test-only field codes `INVALID_SIZE` / `REQUIRED_NOT_BLANK` — already centralized in the
  `FieldError` test helper (`size()` / `notBlank()`); they are starter defaults our production code
  does not emit. Leave them in `FieldError`; don't pull test vocabulary into production constants.
- The yml-configured codes `authentication-failed` / `too-many-login-attempts`
  (`application.yml` → `error.handling.codes`) — configuration, not referenceable from a Java constant.

## Note (recorded, not addressed here)

Casing convention is inconsistent across the two mechanisms: the hand-written / starter-default codes
are `UPPER_SNAKE` (`VALIDATION_FAILED`, `ALREADY_EXISTS`, `INVALID_SIZE`, `REQUIRED_NOT_BLANK`), while
the yml-configured codes are `lower-kebab` (`authentication-failed`, `too-many-login-attempts`). Not
part of this task; flagged for awareness.

## Full-stack journey verdict

`no-impact` — pure structural DRY of internal error-code literals; the rendered RFC 9457 responses
(status, code, message, field errors) are byte-identical before and after. No critical-path change.

## Key Files

- `src/main/java/by/iivanov/rpm/shared/infrastructure/web/errors/ErrorConstants.java` — candidate home
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/web/EmailAlreadyExistsExceptionHandler.java`
- `src/main/java/by/iivanov/rpm/iam/user/infrastructure/web/LoginAlreadyExistsExceptionHandler.java`
- `src/test/java/by/iivanov/rpm/iam/user/infrastructure/web/UserResourceTest.java` — assertions on the codes
