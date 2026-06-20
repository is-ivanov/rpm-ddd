# Task 190: Frontend Audit — Code-Quality Cleanup

Type: refactoring
Issue: #190

## Dependencies

- **Step 2 (runtime response validation) is blocked by #191** — the validation-library decision
  (zod/valibot/vee-validate vs custom) determines how the network boundary is validated. Step 2 is
  deferred and folded into Task 191's implementation. Steps 1, 3, 4 have no dependency and proceed
  independently.
- Step 1 (type-checked ESLint) is **done**.

## Problem

Findings from the senior FE audit (`ProductSpecification/audits/2026-06-20-frontend-audit.md`) that
are pure quality / tech-debt — no behaviour
change required. Grouped into one task with four independent steps.

## Solution

### Step 1 — type-checked ESLint
Move the frontend ESLint config from `recommended` to `recommendedTypeChecked` (add
`parserOptions.project`). This enables `no-floating-promises`, `no-misused-promises`, and the
`no-unsafe-*` family — the root cause that the activation floating-promise and fake-success (#188)
slipped past CI. Fix any violations the new rules surface.

### Step 2 — runtime response validation
Replace blind `as ProblemDetail` / `as ActivationTokenResponse` casts (`login.api.ts`,
`activation.api.ts`) with runtime validation (zod/valibot) at the network boundary, so a
backend/contract drift fails fast instead of reaching the user. (Library choice may depend on #191.)

### Step 3 — design tokens
Remove hardcoded hex (`#228be6`, `#f8f9fa`, `#212529`, `#6c757d`, `#fa5252`, `#dee2e6`, …) from
templates; unify the two colour systems (auth pages = arbitrary hex vs `HomePage`/`NotFoundPage` =
Tailwind palette) into `@theme` tokens; extract the duplicated `RPM` logo. Aligns with our own CSS
Utility Extraction rule.

### Step 4 — dedup
- `LoginPage` inlines its own password input + eye toggle instead of reusing `PasswordField`
  (diverging `pr-9.5`/`pr-10`, `right-2.5`/`right-2`, icon `18`/`16`).
- Duplicate types: `ProblemFieldError` ≡ `LoginFieldError`; identity `toLoginFieldError`.
- Magic route strings (`'/login'`) vs the declared `LOGIN_PATH`.
- Google Fonts `@import` in `style.css` (render-blocking) → `<link rel=preconnect>`.

## Key Files

- `frontend/eslint.config.*` / `frontend/tsconfig*` (Step 1)
- `frontend/src/features/auth/logic/{login.api.ts,activation.api.ts,types.ts}` (Steps 2, 4)
- `frontend/src/style.css`, `@theme` tokens, auth/home templates (Step 3)
- `frontend/src/features/auth/components/{LoginPage.vue,PasswordField.vue}` (Step 4)
