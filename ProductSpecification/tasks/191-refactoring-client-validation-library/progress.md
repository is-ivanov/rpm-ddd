# Task 191: Decide — Client Validation Library vs Custom Validation — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Decision — adopt a validation library or keep custom
- [ ] design (write-up our approach + .claude constraints + senior remark + pros/cons → decide; ADR if adopted)

### Step 2: Implementation (only if adopted)
- [S] (steps defined after the Step 1 decision)
> **Folds in Task 190 Step 2** — when a library is chosen, the same schemas validate the network
> boundary (`login.api.ts` / `activation.api.ts`), replacing the blind `as ProblemDetail` /
> `as ActivationTokenResponse` casts. Add an explicit `red-frontend-api` / `green-frontend-api`
> cycle for that here (the work originally scoped as #190 Step 2). Also underpins #189's
> password-rules + confirm-password validation. If Step 1 decides to **keep custom**, the #190
> Step 2 type-guard work goes back to Task 190 instead.
