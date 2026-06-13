# Red Phase Formats

## Failure Prediction Format (Single Method)

```
PREDICTED FAILURE:
- Type: [CompilationError | AssertionError | Exception]
- Message: "[expected error/assertion message]"
- Reason: "[why this failure is expected]"
```

## Failure Prediction Format (Multiple Methods -- Adapter Tests)

```
PREDICTED FAILURES:
1. {testMethodName}:
   - Type: AssertionError
   - Message: "..."
   - Reason: "..."
2. {testMethodName}:
   - Type: AssertionError
   - Message: "..."
   - Reason: "..."
```

## Output Summary Format

```
## Summary

**Files created:**
- `path/to/TestFile`

**Test method:** `testMethodName`

**Predicted failure:**
- Type: AssertionError
- Message: "expected message"
- Reason: reason

**Actual failure:**
actual error output

**Comparison:**
| Field   | Predicted                  | Actual                     | Match? |
|---------|----------------------------|----------------------------|--------|
| Type    | AssertionError             | AssertionError             | YES    |
| Message | "..."                      | "..."                      | YES    |

**Verdict:** ALL YES → test disable marker added

**Test status:** {tech-specific disable marker with reason}

**Next step:** Implement feature using /green-{layer} command
```

## Domain Field Gate Table

Before writing domain classes, produce this table for each field:

```
| Class.field         | Statements reference (method + line) | Verdict  |
|---------------------|--------------------------------------|----------|
| Column.name         | assertEmptyBoard -> Column.empty("To Do") | KEEP  |
| Column.tasks        | (none -- no Statements line reads .tasks) | REMOVE  |
```

"The recursive comparator compares all fields" is not a valid justification for KEEP -- remove the field so the comparator has nothing extra to compare.

## Prediction Comparison Table

After running the test, write this field-by-field comparison:

```
| Field   | Predicted | Actual | Match? |
|---------|-----------|--------|--------|
| Type    | ...       | ...    | YES/NO |
| Message | ...       | ...    | YES/NO |
| Status  | ...       | ...    | YES/NO |
```

Zero NOs -- add the test disable marker. Any NO -- do NOT disable, fix and re-run.

## Frontend RED-Phase Marker

For frontend and browser-testing layers, use the **test skip marker named in the Conventions table** — its exact name differs per concern, so never hardcode one name across both: Vitest frontend-unit uses `it.fails(...)`; Playwright browser-testing uses `test.fail(...)` (Playwright has **no** `test.fails` — that name throws `TypeError: test.fails is not a function`). It is neither the backend test disable marker nor `.skip`. Add a comment above naming the actual failure reason. Unlike `.skip`, this marker **runs every build**: it stays green while it fails and **fails the build once it passes**, forcing the GREEN-phase marker removal.

This marker has no error-type pin (no `withExceptions` analog) — **pin the RED reason via a specific `expect(...)` inside the test** so an incidental failure isn't absorbed as "expected fail". See `.claude/tech/vue-ts/tdd.md` → "RED-Phase Marker" (Vitest) and `.claude/tech/playwright/tdd.md` → "Test Disable Marker" (Playwright).

## Test Disable Marker Rules

After verified failure, the disable marker message MUST include the actual failure reason (not a generic "TDD Red Phase" label alone).

For adapter test classes with multiple test methods, place the disable marker per the tech binding — some markers are **method-only** (one marker on each `@Test`, not on the class). See the tech binding's `red-phase-formats.md` for placement.
