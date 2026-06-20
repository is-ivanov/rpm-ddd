# Task 190: Frontend Audit — Code-Quality Cleanup — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: type-checked ESLint (recommendedTypeChecked)
- [x] refactor (eslint config + fix surfaced violations)

### Step 2: runtime response validation (replace blind `as`)
> **Done under #191 — zod adopted.** #191 chose zod and validated the network boundary as part of
> its Step 2 (explicitly "folds in Task 190 Step 2"): the blind `as ProblemDetail` /
> `as ActivationTokenResponse` casts in `login.api.ts` / `activation.api.ts` were replaced with
> `problemDetailSchema` / `activationTokenResponseSchema`. The "keep custom validation" branch did
> not occur, so nothing lands here.
- [S] red-frontend-api (done under #191 — zod network-boundary validation)
- [S] green-frontend-api (done under #191 — zod network-boundary validation)

### Step 3: design tokens (remove hex, unify palette, extract logo)
- [x] align-design
- [x] refactor (cleanup)

### Step 4: dedup (PasswordField, types, routes, fonts)
- [~] refactor (cleanup)
