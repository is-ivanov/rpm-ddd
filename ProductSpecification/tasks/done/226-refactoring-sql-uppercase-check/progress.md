# Task 226: Static-analysis check for UPPER_CASE SQL/JPQL keywords -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Prototype the check mechanism
- [x] refactor (prototyped Checkstyle RegexpMultiline + assessed Error Prone; measured false positives — see findings.md; chose anchored Checkstyle rule)

### Step 2: Wire the chosen check into the build (or document review-only)
- [x] refactor (ported two anchored RegexpMultiline modules into my_checks.xml; fixed UserSummaryView @Subselect to SELECT … FROM)
- [x] green-acceptance (checkstyle:check green with the rule active; UserGridIntegrationTest passes — @Subselect view works with UPPER_CASE keywords)
