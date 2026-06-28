# Task 244: Extend the PMD ruleset (enable all categories, burn down violations)

Type: refactoring
Issue: #244

## Problem

The project's PMD ruleset (`code-quality-config/pmd/ruleset.xml`) used to reference
`rulesets/java/maven-pmd-plugin-default.xml` — the minimal legacy set that maven-pmd-plugin
ships "out of the box" (empty catch/if/while/try blocks, unused imports/variables, etc.).
That is neither the recommended nor a remotely complete rule set — effective PMD coverage was
close to zero. PMD 7 splits every rule across 8 categories and has no single "recommended"
set.

## Solution

Enable **all 8 PMD categories** in the ruleset (full coverage), then burn the violation
count down to zero **in batches** rather than one mega-commit:

- The ruleset now references all categories: `bestpractices`, `codestyle`, `design`,
  `documentation`, `errorprone`, `multithreading`, `performance`, `security`.
- `pom.xml` (`maven-pmd-plugin`) carries a `<maxAllowedViolations>` ceiling so the build stays
  green while the count is still above zero. The ceiling is **only ever lowered**, never raised.
- Each batch (one `/continue` work unit) takes a slice of the outstanding violations and either:
  - **fixes** the real ones in code (e.g. `SystemPrintln`, `PreserveStackTrace`), or
  - **disables / configures** rules that are pure noise, duplicate an already-wired tool
    (Spotless/Palantir format, Checkstyle, SpotBugs + find-sec-bugs), or contradict an
    established project convention (e.g. `MethodNamingConventions` vs. the `should_*` test
    naming, `UseExplicitTypes` vs. `var` usage).
- After every batch, re-run `./mvnw pmd:check -B`, read the actual remaining count, and set
  `<maxAllowedViolations>` to exactly that number. The ceiling ratchets down monotonically until
  it reaches 0, at which point the ceiling can be removed.
- Keep the existing override `TooManyStaticImports` = 7 (test DSLs lean on static imports).

## Triage policy (per rule)

- **Disable** — the rule is owned by another tool (formatting → Spotless/Palantir; naming/structure
  → Checkstyle; security/null → SpotBugs/find-sec-bugs/NullAway) or it is style-only noise the
  project does not adopt (`final` on params/locals, comment-size, default-access comments).
- **Configure** — the rule is good but its defaults clash with a project convention; tune the
  property instead of disabling (e.g. `MethodNamingConventions.junit5TestPattern` to allow `_`).
- **Fix** — a genuine defect or smell with a small, in-scope fix.
- **Defer** — a real but large finding goes to a follow-up issue, not this task.

## Key Files

- `code-quality-config/pmd/ruleset.xml` — category refs, excludes, property overrides.
- `pom.xml` — `maven-pmd-plugin` `<maxAllowedViolations>` ceiling (ratchets down per batch).