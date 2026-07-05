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

### Batch 2b — formatting/convention noise — DONE, ceiling 830 → 315
Two sub-batches, both discussed per-rule with the user:

* `UseExplicitTypes` (157, disabled) — project adopted `var` (157 usages / 54 files); no tool
  forbids it. Ceiling 830 → 673.
* Three more codestyle rules (ceiling 673 → 315), all disabled:
  - `LongVariable` (136) — 17-char cap forces abbreviating descriptive names
    (`registrationPolicy`, `emailNotificationSender`); contradicts naming convention; nothing else
    caps length.
  - `CommentDefaultAccessModifier` (136) — wants `/* package */` on package-private members; the
    modifier is self-documenting and boundaries are enforced by Modulith + ArchUnit; forced comments
    clash with the no-comments-unless-asked rule.
  - `MethodNamingConventions` (86, all `should_*`/`when_*` test methods) — duplicates Checkstyle
    `MethodName`, already `@Test`-aware via `SuppressionXpathSingleFilter`.

- [x] refactor (codestyle excludes + ceiling 315 + pmd:check green)

### Batch 3 — structural & naming style noise (5 disabled, 5 to configure) — ceiling 315 → 170
Discussed per-rule with the user. First five disabled:
- `AtLeastOneConstructor` (69, codestyle) — flags stateless/static-method + Spring @Configuration classes.
- `CallSuperInConstructor` (23, codestyle) — Java auto-inserts no-arg super(); all violations in test classes.
- `AvoidFieldNameMatchingMethodName` (20, errorprone) — record component false positives (auto-accessor).
- `LawOfDemeter` (17, design) — 9/17 enum-constant accesses (UserStatus.PENDING); rest fluent/library API.
- `OnlyOneReturn` (16, codestyle) — single-return not adopted; guard clauses idiomatic.
- [x] refactor (codestyle/design/errorprone excludes + ceiling 170 + pmd:check green)

### Batch 3b — remaining 5: explore configuration (33 violations)
`ShortVariable` (13), `LinguisticNaming` (13), `ShortMethodName` (4), `ShortClassName` (1),
`ConfusingTernary` (2). Each needs targeted config/exception instead of blanket disable.

* `ShortVariable` (13 → 0, ceiling 170 → 157) — configured `violationSuppressRegex` to allow-list
  conventional short names `(id|bd)` (name appears in the message; precise vs lowering `minimum`);
  renamed the one genuine 1-char smell `p` → `parser` in StringTrimmerJacksonDeserializer.
- [x] refactor: ShortVariable (violationSuppressRegex allow-list + rename p→parser, ceiling 157)

Remaining candidates (config approach chosen per rule, still to apply):
`LinguisticNaming` (13 — narrow via checkBooleanMethod/checkGetters=false), `ShortMethodName`
(4 — `minimum=2`, name not in message so regex can't target), `ShortClassName` (1 —
violationSuppressRegex allow `User`), `ConfusingTernary` (2 — no useful property; exclude).
- [ ] refactor (configure remaining 4 + lower ceiling + verify)

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