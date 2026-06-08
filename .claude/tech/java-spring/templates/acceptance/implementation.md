# Acceptance Test Implementation (Green Phase) -- Java/Spring

> Universal workflow: `.claude/templates/tdd/green-acceptance.md`

## Tech-Specific Details

- **RED marker**: `@ExpectedToFail(withExceptions = ...)` -- remove to enable at GREEN. The test runs every build under the marker (aborts while it fails with the predicted exception); once the implementation makes it pass, the build FAILS until the marker is removed. If it still fails after removal, STOP and report (do not re-add) — the implementation is incomplete.
- **Test target**: `{TestClassName}` (class name for test runner filter)
- **Error terminology**: "exception handlers" (`@ControllerAdvice`)
