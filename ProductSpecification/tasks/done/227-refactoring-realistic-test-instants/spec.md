# Task 227: Realistic test instants

Type: refactoring
Issue: #227  <- refactoring records the issue for traceability; tests are NOT tagged with it

## Problem

Many tests and test-data fixtures use arbitrary "some moment" timestamps zeroed to the
minute/second (e.g. `…T12:00:00Z`, `…T00:00:00Z`). Real-world timestamps almost never land on a
whole minute; such values read as placeholders and can hide precision bugs that only surface with
sub-second values (truncation, rounding, formatting, equality after a serialization round-trip).

A TDD rule was added for this (`.claude/rules/tdd-rules.md` → "Time-Dependent Behavior":
*"Arbitrary test instants must be realistic — non-round"*). This task brings existing tests and
fixtures in line with it.

## Solution

Sweep test sources and seed/fixture data for arbitrary zeroed instants and replace them with
realistic values carrying minutes, seconds, and fractional seconds (e.g. `…T12:34:17.482Z`). Keep
dependent assertions/fixtures consistent (a fixture instant change must be mirrored in the
expected-response fixture and any literal assertion). Behavior-preserving — tests stay green after
each change.

**Exclusions (do NOT change — document each in the commit):** instants whose round value is the
semantic point — a tested boundary (exactly midnight, an exact interval edge) or a value asserted
precisely because it is a boundary; and already-realistic values such as
`2026-01-05T10:23:56.632Z` (`SharedTestClockConfiguration`, `ResubmitIncompletePublicationsJobTest`).

## Key Files

- `src/test/**/*.java` — `Instant.parse(...)`, `LocalDate/LocalDateTime` constants,
  `MutableClock.of(...)` baselines (e.g. `ActivationServiceTest` `2026-01-01T00:00:00Z`).
- `src/test/resources/db/data/*.csv` — seed timestamps (e.g. `user.csv`).
- `src/test/resources/__files/**/*.json` — expected-response fixtures (e.g. `listUsers_out.json`
  rows like `2026-01-01T00:00:00Z`).
- `src/test/resources/db/changelog/*` — Liquibase/SQL test changelogs, if any carry zeroed instants.

## Full-Stack Journey

Verdict: **no-impact** — test-data only, no rendered critical path.
