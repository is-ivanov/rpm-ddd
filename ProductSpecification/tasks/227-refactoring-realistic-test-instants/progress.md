# Task 227: Realistic test instants -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Replace zeroed arbitrary instants in Java test literals (src/test)
> Scan `Instant.parse(...)`, `MutableClock.of(...)` baselines, and date/time constants. Replace
> zeroed arbitrary values with realistic non-round ones; keep boundary/semantic values and document
> each exclusion. Update any literal assertion that mirrors a changed value. Run affected tests.
- [ ] refactor (java test instants)

### Step 2: Replace zeroed arbitrary instants in seed/fixture data and mirror dependents
> Scan `src/test/resources/db/data/*.csv`, `src/test/resources/__files/**/*.json`, and Liquibase/SQL
> test changelogs. Replace zeroed seed/fixture instants; mirror every change in the matching
> expected-response fixture and assertions. Keep boundary values; document exclusions. Run the full
> test suite green.
- [ ] refactor (fixture data)
