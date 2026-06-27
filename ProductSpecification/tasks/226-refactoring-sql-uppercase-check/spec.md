# Task 226: Static-analysis check for UPPER_CASE SQL/JPQL keywords

Type: refactoring
Issue: #226

## Problem

The convention "SQL/JPQL keywords are UPPER_CASE" lives in `coding-rules.md` (SQL & JPQL) but is
enforced by review only.

## Solution

Prototype an automated check. PMD/Checkstyle cannot parse SQL semantically inside Java string
literals; a Checkstyle `RegexpMultiline` over `@Query`/`.sql(...)`/string literals is possible but
false-positive-prone (keywords used as identifiers or in comments). An Error Prone custom check could
target `@Query` annotations and `JdbcClient`/`JdbcTemplate` call sites more precisely. Prototype the
candidates, measure false positives, and pick the mechanism — or conclude review-only is sufficient
and document that.

Covers Story 4 review Q4 (static check). Backlog ref: Story 4 improvements.md I5.

## Key Files

- `code-quality-config/checkstyle/my_checks.xml`
- `pom.xml` (Error Prone / Checkstyle plugin config)
