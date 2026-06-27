# Task 228: Validation test helper

Type: refactoring
Issue: #228  <- the task number IS the issue number; refactoring records it for traceability and does NOT tag tests

## Problem

The request-DTO validation unit-test pattern is now duplicated across `ActivateAccountRequestTest`
and `RegisterUserRequestTest` (both in `iam.user.infrastructure.web`): a valid Instancio model, one
"no violations" `@Test`, a `@ParameterizedTest` over `argumentSet(...)` invalid cases asserted with
`org.hibernate.validator.testutil.ConstraintViolationAssert`, plus private
`blankCase`/`sizeCase`/`notBlank`/`size`/`emailFormat` helpers. Per the new "every request DTO needs
a validation unit test" rule, this boilerplate will be copied into every new request-DTO test.

## Solution

Extract a shared test utility (under `by.iivanov.rpm.testing`) that makes building these
`argumentSet` constraint-violation cases concise and reusable — `NotBlank` / `Size` / `Email` /
composite (`@RequiredString`) violations, including the combined-violation cases (`@Email` also fires
on a blank-whitespace value and on an over-length value, so an `email` blank/too-long case expects two
violations). Refactor both existing tests onto the helper.

Establish/enforce the convention that boundary limits are pinned as **literals** in the test (the
production constant named only in a comment), NOT derived from production constants like
`Login.MAX_LENGTH` / `EmailAddress.MAX_LENGTH` / `PasswordPolicy.MIN_LENGTH`. Deriving both the
boundary input AND the expected message from a production constant makes a boundary test tautological:
it silently follows a limit change instead of failing on it (per `tdd-rules.md` — tests must be dumb,
never derive expected values from production logic). `RegisterUserRequestTest` was already converted to
literals; `ActivateAccountRequestTest` still derives from `PasswordPolicy` and must be brought onto the
literal convention as part of this work.

## Key Files

- `src/test/java/by/iivanov/rpm/iam/user/infrastructure/web/RegisterUserRequestTest.java`
- `src/test/java/by/iivanov/rpm/iam/user/infrastructure/web/ActivateAccountRequestTest.java`
- new shared helper under `src/test/java/by/iivanov/rpm/testing/`
