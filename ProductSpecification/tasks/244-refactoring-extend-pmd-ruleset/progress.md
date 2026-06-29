# Task 244: Extend the PMD ruleset (all categories, burn down in batches) -- Progress

Type: refactoring

Each batch = one `/continue` work unit: take a slice of the outstanding PMD violations,
disable/configure/fix per the triage policy in `spec.md`, re-run `./mvnw pmd:check -B`, set
`<maxAllowedViolations>` in `pom.xml` to the new actual count, verify green, commit.

## Setup
- [x] spec (rewritten in English; batch-based burn-down approach)
- [x] baseline: ruleset references all 8 categories; `pom.xml` ceiling = 2005 (current build green)

## Burn-down batches

### Batch 1 — `final`-modifier rules (796) — DONE, ceiling 2005 → 1209
Discussed per-rule with the user; both disabled (no stack tool enforces `final`; Palantir
does not add it; convention not adopted):
`MethodArgumentCouldBeFinal` (519, disabled), `LocalVariableCouldBeFinal` (277, disabled).
- [x] refactor (codestyle excludes + ceiling 1209 + pmd:check green)

### Batch 2 — `CommentSize` (379) — DONE, ceiling 1209 → 830
Discussed per-rule; disabled. Line length (352) is owned by Checkstyle `LineLength`=120
(applies to comments) + `.editorconfig`=120 — PMD's 80-char default conflicts with the adopted
120; the 6-line cap (27) conflicts with the project rule to preserve Javadoc/comments.
- [x] refactor (documentation exclude CommentSize + ceiling 830 + pmd:check green)

### Batch 2b — remaining formatting/convention noise (to discuss per-rule)
Candidates: `UseExplicitTypes` (157 — conflicts with project `var` usage),
`CommentDefaultAccessModifier` (136), `LongVariable` (136),
`MethodNamingConventions` (86 — duplicates Checkstyle `MethodName`, which is already
`@Test`-aware via SuppressionXpathSingleFilter). Triage disable vs configure per rule.
- [ ] refactor (ruleset excludes/config + lower ceiling + verify)

### Batch 3 — structural & naming style noise
Candidates: `AtLeastOneConstructor` (69), `CallSuperInConstructor` (23),
`AvoidFieldNameMatchingMethodName` (20), `LawOfDemeter` (17), `OnlyOneReturn` (16),
`ShortVariable` (13), `LinguisticNaming` (13), `ShortMethodName` (4), `ShortClassName` (1),
`ConfusingTernary` (2). Triage disable vs configure per rule.
- [ ] refactor (ruleset excludes/config + lower ceiling + verify)

### Batch 4 — test-rule tuning
Candidates: `UnitTestShouldIncludeAssert` (46), `UnitTestContainsTooManyAsserts` (12),
`TestClassWithoutTestCases` (4), `UnitTestShouldUseTestAnnotation` (2). Configure for the
project's Statements/DSL test style or disable where duplicated by convention.
- [ ] refactor (ruleset excludes/config + lower ceiling + verify)

### Batch 5 — real fixes + long tail
Fix genuine findings: `SystemPrintln` (5), `PreserveStackTrace` (1), `GuardLogStatement` (10),
`MissingSerialVersionUID` (7), `AvoidDuplicateLiterals` (4), and the remaining long-tail rules.
Fix in code where small/in-scope; defer larger ones to follow-up issues; disable true noise.
- [ ] refactor (fix code / curate ruleset + lower ceiling + verify)

## Final
- [ ] green-acceptance (`./mvnw verify -B` green; ceiling at its final value, ideally 0)