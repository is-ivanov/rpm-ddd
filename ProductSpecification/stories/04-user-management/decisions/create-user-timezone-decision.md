# Decision: User time zone is the core-Java `ZoneId` in the domain; web validates the raw string with jakarta bean validation

**Date**: 2026-06-27 **Scenarios**: 3.1 (introduces it), 5.5 (consumes the validation placement)

How to thread a user's `timeZone` through create-user
(`RegisterUserRequest` → `RegisterUserCommand` → `User.register(...)` → persistence),
and — decided here, not in 5.5 — **where its IANA-validity is enforced**.

## Chosen

- **Domain type is `java.time.ZoneId`.** `ZoneId` is core Java (not a framework type), immutable,
  and self-validating via `ZoneId.of(...)` — a legitimate value object for the "time zone" concept,
  so no bespoke `TimeZone` wrapper VO is introduced.
  - `RegisterUserCommand` gains `ZoneId timeZone`.
  - `User` gains a `ZoneId timeZone` field; `User.register(...)` takes a `ZoneId` parameter and stores it.
  - Hibernate 6 persists `ZoneId` as its string zone-id natively (built-in `ZoneId` JavaType → VARCHAR) —
    no `AttributeConverter` needed.
- **Web layer keeps the raw `String`, non-null, validated with jakarta bean validation.**
  - `RegisterUserRequest.timeZone` is a `String` — `@NotBlank` (the create modal always pre-fills it,
    FE Scenario 4.1), `@Size(max = 64)`.
  - `RegisterUserRequest.toCommand()` converts `ZoneId.of(timeZone)` (request DTOs own their conversions);
    because bean validation runs before the handler body, `toCommand()` only ever sees an already-valid string.
  - **IANA-validity (Scenario 5.5) is a custom jakarta constraint on the DTO field** (e.g. checking
    `ZoneId.getAvailableZoneIds().contains(value)`), surfacing as a standard 422 field error — the same
    path as the existing `beanValidationTest`. This is the decision's forward impact: **5.5 is a web-slice
    (L2) validation, not a domain VO** → `red-domain` / `green-domain` stay `[S]` in both 3.1 and 5.5.

## Rejected

| Rejected | Why |
|----------|-----|
| **Bespoke `TimeZone` value object** (`@ValueObject record TimeZone(String)` validating in its ctor → `DomainValidationException`, 5.5 reuses the duplicate-login → 422 `ApiExceptionHandler` pattern) | `ZoneId` already *is* an immutable, self-validating core-Java value type for exactly this concept — wrapping it adds a redundant domain class. Validity is a simple membership check best expressed as a jakarta constraint at the boundary, yielding the conventional `fieldErrors` 422 with no custom exception handler. |
| **Raw `String` end-to-end** (String in command + `User`; validity via DTO constraint only) | Loses the typed, immutable domain representation — domain code would re-parse the string everywhere it needs a zone. `ZoneId` as the domain type costs nothing extra and is strictly better. |

## Model / Foundation (this scenario, 3.1)

- `RegisterUserCommand` → add `ZoneId timeZone` (non-null `Checks.notNull`).
- `RegisterUserRequest` → `timeZone` becomes `@NotBlank @Size(max = 64) String` (was `@Nullable` plumbing);
  `toCommand()` builds `ZoneId.of(timeZone)`.
- `User` → add `ZoneId timeZone` field; `User.register(...)` gains a `ZoneId` parameter; `UserRegistrationService`
  passes `command.timeZone()`.
- Migration: add `time_zone varchar(64)` to `iam_user` — nullable → backfill existing rows with `'UTC'` →
  NOT NULL (mirrors the audit-columns migration `2026.06.27-01`). Postgres has **no** dedicated zone-id type
  (`timestamptz` stores an instant, not a zone), so a `varchar` column holding the IANA id is canonical.

## Test layering

- `red/green-usecase` (3.1): command carries `ZoneId` → `User.register` stores it → fake-repo captures →
  strict assert on the stored zone. This is the real RED for 3.1 (no L1-observable surface for a PENDING
  user's time zone — see the red-acceptance decision).
- `red/green-domain` (3.1): **`[S]`** — `timeZone` is a plain stored field, no domain branch/logic.
- 5.5 (consumes this decision): web-slice (L2) — custom jakarta constraint → 422 field error; domain steps `[S]`.
