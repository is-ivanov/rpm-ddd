# Task 226: Static-analysis check for UPPER_CASE SQL/JPQL keywords -- Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Prototype the check mechanism
- [ ] refactor (try Checkstyle RegexpMultiline and/or Error Prone custom check; measure false positives)

### Step 2: Wire the chosen check into the build (or document review-only)
- [ ] refactor (enable in pom/checkstyle config, or record the decision if not feasible)
- [ ] green-acceptance (build green with the check active on existing code)
