# Task 190: Frontend Audit — Code-Quality Cleanup — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: type-checked ESLint (recommendedTypeChecked)
- [x] refactor (eslint config + fix surfaced violations)

### Step 2: runtime response validation (replace blind `as`)
> **Deferred — depends on #191** (validation-library decision is still open). The library choice
> determines how we validate the network boundary, so this step is folded into Task 191's
> implementation (see `191/progress.md` Step 2). Revisit here only if #191 decides to keep custom
> validation, in which case hand-written type guards land in this task.
- [S] red-frontend-api (deferred → done under #191 once the library is chosen)
- [S] green-frontend-api (deferred → done under #191 once the library is chosen)

### Step 3: design tokens (remove hex, unify palette, extract logo)
- [ ] align-design
- [ ] refactor (cleanup)

### Step 4: dedup (PasswordField, types, routes, fonts)
- [ ] refactor (cleanup)
