# Task 141: RED-phase marker → junit-pioneer @ExpectedToFail (backend)

Type: refactoring
Issue: #141  <- enhancement issue (not a bug); tracks this framework/docs change

## Problem

Our backend TDD workflow predicts the exact RED failure (type + message + status),
runs to validate it, then commits the failing test behind `@Disabled`. `@Disabled`
has two weaknesses:

1. **RED state is not machine-verified** — once disabled, CI never re-confirms the
   test still fails for the predicted reason; it is dead weight until GREEN.
2. **The GREEN transition is not enforced** — GREEN means manually removing
   `@Disabled`; forget, and nothing notices.

## Solution

Adopt junit-pioneer's `@ExpectedToFail` as the backend RED-phase marker, with a
**mandatory `withExceptions`** attribute pinning the predicted exception type:

| Event | `@ExpectedToFail` behavior |
|---|---|
| Test fails with a listed exception | aborted (reported skipped) — build stays green |
| Test passes | build **FAILS** — forces marker removal at GREEN |
| Other failure (e.g. infra connection refused) | real failure — not silently swallowed |

`withExceptions` is mandatory: without it junit-pioneer aborts on *any* failure,
including infrastructure errors, violating our rule "predictions must be about
feature behavior, not infrastructure." Pinning the predicted type makes an infra
failure a real failure — this is the core value.

**Scope:** all backend levels — usecase, domain, web-slice, AND acceptance (Level 1).

**No test migration needed:** there are no live `@Disabled` annotations in Java
today (all prior RED tests are already green). This is a dependency add + docs /
templates / agents update + a throwaway demonstrative test.

### Decision: demonstrative test is throwaway

The acceptance-criteria "demonstrative RED test" is written to prove the mechanism
(aborts on the predicted exception → build green; passes → build fails), then
**removed**. We do not commit a permanently-aborting example test — that would
pollute the suite with a perpetual skip. The proof is the verification.

## Key Files

- `pom.xml` — add `org.junit-pioneer:junit-pioneer` (latest 2.x) test dependency
- `ProductSpecification/technology.md` — Conventions table: `Test disable marker`
- `.claude/tech/java-spring/tdd.md` — marker description + RED/GREEN mechanics
- `.claude/tech/java-spring/templates/testing/red-phase-formats.md` — syntax + examples
- `.claude/tech/java-spring/templates/{usecase,acceptance,db,rest}/test-class.md`
  and `.claude/tech/java-spring/templates/acceptance/implementation.md`
- `.claude/agents/red-agent.md`, `.claude/agents/green-agent.md` — marker wording
- Universal references to "test disable marker" — only where changed *semantics*
  (test now runs every build) matter; the abstract term itself may stay

## Acceptance

- A demonstrative RED test using `@ExpectedToFail(withExceptions = ...)` aborts on
  the predicted exception (build green) and FAILS the build once the code works
  (forcing marker removal). Verified, then removed.
- All framework docs/templates/agents reference `@ExpectedToFail` with mandatory
  `withExceptions`.
- `./mvnw verify -B` is green with junit-pioneer on the classpath.

## Related

- Frontend counterpart (Vitest `it.fails()`): #142. Deferred — land this first.
