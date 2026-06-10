# Task 153: Enforce @NullMarked package-info on every main package via an ArchUnit rule

Type: refactoring
Issue: #153

## Problem

The project relies on JSpecify `@NullMarked` package-info files to null-mark production packages,
but nothing prevents a newly added `src/main` package from shipping without a `@NullMarked`
package-info. Today `src/main` is ~100% covered (19 packages, 20 package-info files) — but that
coverage is unenforced and can silently regress.

## Solution

Add an ArchUnit rule (extend the existing `ArchitectureTest`) asserting that **every `src/main`
package containing classes has a `package-info.java` annotated with
`org.jspecify.annotations.NullMarked`**. A new main package added without the annotation must fail
the build.

### Background — why this does not change build-time null safety

NullAway already enforces nullness across all of `by.iivanov.rpm` (main **and** test) via
`-XepOpt:NullAway:AnnotatedPackages=by.iivanov.rpm` in `pom.xml`, and the error-prone compiler
plugin runs on `testCompile` as well as `compile`. This rule therefore does **not** change
build-time null enforcement — it locks in the `@NullMarked` package-info convention, which:

- drives IntelliJ's own nullability inspections (which read `@NullMarked`, not `AnnotatedPackages`);
- documents intent at the package boundary;
- is a prerequisite if we ever migrate from `AnnotatedPackages` to NullAway JSpecify mode
  (`-XepOpt:NullAway:JSpecifyMode=true`).

Since main is already nearly fully covered, the rule mainly prevents regressions.

## Scope

- **In scope:** `src/main` packages only; one ArchUnit rule in `ArchitectureTest`.
- **Out of scope:** `src/test` packages — do NOT require package-info there. Deliberate-null tests,
  Mockito mocks, and framework-interface fakes would add IntelliJ-warning churn for no added safety,
  since `AnnotatedPackages` already covers test code at build time.

The ArchUnit rule must restrict its scope to main classes — it must not require `@NullMarked` on
test packages even though `ArchitectureTest` itself runs from `src/test`.

## Key Files

- `src/test/java/by/iivanov/rpm/ArchitectureTest.java` — add the new rule here (existing ArchUnit suite)
- `src/main/java/by/iivanov/rpm/**/package-info.java` — the convention being enforced (reference; already compliant)
- `pom.xml` — NullAway `AnnotatedPackages` config (reference; explains why build-time safety is unchanged)

## Context

Discovered while fixing #148 (a test fake required `@Nullable`/`@NullMarked` annotations), which
surfaced the unenforced convention.
