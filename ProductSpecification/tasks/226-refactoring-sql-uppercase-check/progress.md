# Task 226: Static-analysis check for UPPER_CASE SQL/JPQL keywords -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Prototype the check mechanism
- [x] refactor (prototyped Checkstyle RegexpMultiline + assessed Error Prone; measured false positives — see findings.md; chose anchored Checkstyle rule)

### Step 2: Wire the chosen check into the build (or document review-only)
- [~] refactor (port the anchored RegexpMultiline into my_checks.xml; fix UserSummaryView @Subselect keywords to UPPER_CASE)
- [ ] green-acceptance (build green with the check active on existing code)
