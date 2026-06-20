# Task 191: Decide — Client Validation Library vs Custom Validation — Progress

Type: refactoring

## Spec
- [x] spec

## Fix

### Step 1: Decision — adopt a validation library or keep custom
- [x] design (decision: **adopt zod**; ADR `decisions/client-validation-library-decision.md`)

### Step 2: Implementation — adopt zod + validate the network boundary
> **Folds in Task 190 Step 2**: the schemas validate the network boundary
> (`login.api.ts` / `activation.api.ts`), replacing the blind `as ProblemDetail` /
> `as ActivationTokenResponse` casts. #189's password-rules + confirm-password validation builds on
> this convention but lands in Task #189, not here.

- [x] setup — `npm install zod` (4.4.3); schema-storage convention added to the vue-ts tech binding
  (`.claude/tech/vue-ts/coding.md` → "Schema Validation (zod)"); added
  `src/app/schemas/problem-detail.schema.ts` (RFC 9457 shape)
- [x] red-frontend-api (runtime response validation) — `it.fails` test in `activation.api.test.ts`:
  a 200 with a schema-violating body (`{ login }`, no `email`) must reject; today the blind `as`
  cast resolves it. Prediction matched 3/3. (`login.api.ts` boundary follows in green.)
- [x] green-frontend-api — `activation-token.schema.ts` created; `validateActivationToken` now
  `safeParse`s the success body (malformed → `ActivationError`), and both `activation.api.ts` +
  `login.api.ts` parse the error body via `problemDetailSchema` (retiring the blind `as` casts and
  the dead setup schema); `types.ts` migrated to `z.infer` re-exports. Auth suite: 20 passed.
- [~] refactor login-form validation — replace `isLoginFormValid` with a zod-backed validator only
  if it improves clarity; otherwise `[S]` (trivial non-empty check, no behavior change)
