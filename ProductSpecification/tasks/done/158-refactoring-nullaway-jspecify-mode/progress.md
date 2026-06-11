# Task 158: Enable NullAway JSpecifyMode for generic type nullability checks -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Add JSpecifyMode=true to NullAway compiler args
- [x] refactor (add -XepOpt:NullAway:JSpecifyMode=true to pom.xml; verify clean compile and that the flag has teeth)

Outcome notes:
- `./mvnw clean test-compile` (main + test) green with the flag — zero new violations.
- Teeth verified with a temporary class: `List<String> ← List<@Nullable String>` assignment failed
  compilation with `[NullAway] incompatible types`; temp class removed, clean compile re-verified.
- Observed limitation: dereference of a substituted nullable type argument
  (`list.get(0).toUpperCase()` on `List<@Nullable String>`) is NOT flagged by the current NullAway
  JSpecify mode — only generic-type incompatibilities (assignments/returns/overrides) are caught.
