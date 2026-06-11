# Task 153: Enforce @NullMarked package-info on every main package via an ArchUnit rule -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Add the @NullMarked package-info ArchUnit rule (main packages only)
- [x] refactor (add ArchUnit rule in ArchitectureTest: every main package with classes has a @NullMarked package-info)

Outcome notes:
- The "main is already ~100% compliant" assumption in the spec was WRONG: 5 main packages had classes
  but no package-info at all (`shared.infrastructure`, `shared.infrastructure.scheduling`,
  `shared.infrastructure.web`, `shared.time.infrastructure`, `shared.web.errors`). Added a
  `@NullMarked` package-info to each (no jMolecules ring annotations — out of scope, and adding
  `@InfrastructureRing` to `shared.infrastructure` could break the onion rule because domain/application
  stereotype annotations live there).
- Rule implemented as `@ArchTest classesShouldBeNullSafe`; class-level `@AnalyzeClasses` now uses
  `ImportOption.DoNotIncludeTests`, which also narrows the existing jMolecules rules to main classes
  (they still pass; test classes don't need DDD/onion checks).
- Semantics deliberately RELAXED vs the original issue text: a class is null safe if its package has
  a `@NullMarked` package-info OR the class itself is annotated `@NullMarked`. A single-class package
  may annotate the class directly; once a package holds several classes, prefer lifting the
  annotation to a package-info. Known strictness quirk: ArchUnit checks direct annotations only, so a
  nested class inside a `@NullMarked` outer class in an unmarked package is still flagged even though
  JSpecify covers it — resolution is to move the annotation to the package level.
- Teeth verified both ways: removed `@NullMarked` from one package-info → FAIL naming each class of
  the package; deleted the package-info entirely → same FAIL; reverted → 5/5 green.

Notes for the fix session:
- Add the rule to the existing `src/test/java/by/iivanov/rpm/ArchitectureTest.java` (do not create a
  new test class). Reuse its `@AnalyzeClasses` import scope if it already targets `by.iivanov.rpm`.
- **Scope strictly to main.** `ArchitectureTest` runs from `src/test`, so the imported classes may
  include test classes. The rule must NOT require `@NullMarked` on test packages. Restrict via the
  import-location filter (e.g. `ImportOption.DoNotIncludeTests`) and/or a package predicate, and
  verify the rule does not flag any `src/test` package.
- The rule must enforce BOTH presence and annotation: every main package that contains classes has a
  `package-info` AND that package-info is annotated `org.jspecify.annotations.NullMarked`. Checking
  only that existing package-info files are marked is insufficient — a package with no package-info
  at all must fail.
- main is already ~100% compliant (19 packages, 20 package-info), so the rule MUST pass immediately
  on the current codebase.
- **Verify the rule has teeth** (the red analog): temporarily remove `@NullMarked` from one main
  package-info (or delete one), confirm `ArchitectureTest` FAILS with a clear message naming the
  offending package, then revert. Do NOT commit the temporary change.
- Pre-commit: run `./mvnw test -Dtest=ArchitectureTest`, then checkstyle + PMD on the changed file
  and IDE inspections on `ArchitectureTest.java`.
- Pure test-infrastructure addition — no production code changes expected.
