# Task 158: Enable NullAway JSpecifyMode for generic type nullability checks

Type: refactoring
Issue: #158

## Problem

NullAway currently runs with `-XepOpt:NullAway:AnnotatedPackages=by.iivanov.rpm` (pom.xml), which
enforces nullness on fields, parameters, returns and dereferences — but **ignores JSpecify type-use
annotations on generic type arguments**. Code like assigning a `List<@Nullable String>` to a
`List<String>` compiles without complaint.

## Solution

Add `-XepOpt:NullAway:JSpecifyMode=true` to the NullAway compiler args in `pom.xml`. This enables
full JSpecify semantics, most importantly nullability checks on generic type arguments
(assignments, returns, overrides).

Verified before enabling:
- the whole codebase (main + test) compiles clean with the flag — zero new violations, so enabling
  is free;
- the flag is actually active: a deliberate `List<String> ← List<@Nullable String>` assignment fails
  compilation with `[NullAway] incompatible types: List<@Nullable String> cannot be converted to
  List<String>` (this check exists only in JSpecify mode).

### Known limitation

NullAway's JSpecify mode currently catches generic-type incompatibilities but does **not** flag
dereference of a substituted nullable type argument (e.g. `list.get(0).toUpperCase()` on a
`List<@Nullable String>`). The mode is still maturing upstream; coverage will improve with NullAway
upgrades.

## Scope

- **In scope:** one compiler-arg line in `pom.xml`.
- **Out of scope:** migrating from `AnnotatedPackages` to `OnlyNullMarked` — rejected for now
  because `AnnotatedPackages` also covers `src/test` at build time, while test packages deliberately
  carry no `@NullMarked` (see #153 scope).

## Key Files

- `pom.xml` — NullAway compiler args (maven-compiler-plugin `compilerArgs`)

## Context

Follow-up to #153 (ArchUnit rule enforcing the `@NullMarked` convention), whose background named
JSpecify mode as a possible next step.
