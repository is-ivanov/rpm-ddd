# Task 153: Enforce @NullMarked package-info on every main package via an ArchUnit rule -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Add the @NullMarked package-info ArchUnit rule (main packages only)
- [ ] refactor (add ArchUnit rule in ArchitectureTest: every main package with classes has a @NullMarked package-info)

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
