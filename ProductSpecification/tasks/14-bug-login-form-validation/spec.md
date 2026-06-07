# Task 14: Login Form Validation and Forgot-Password Cleanup

Type: bug
Issue: #131

## Problem

Two Story 1 spec omissions found during prod QA of the login page (not regressions):

1. **No client-side required-field validation.** "Sign In" can be clicked with **both fields
   empty**, or with **only the username** filled. Nothing in the mockup or UI test spec guards this.
2. **Dead "Forgot password?" placeholder.** The login page renders "Forgot password?" as a
   non-functional element (`aria-disabled`, `href="#"`). Password reset / forgot-password is
   **explicitly out of scope** for Story 1 (`01_UserLogin_Notes.md:26`, `interview.md:15`), so the
   element should not be shown — it implies a feature that does not exist.

## Solution

1. Add client-side required-field validation: keep **Sign In disabled** (or show per-field
   "required" feedback) until both username and password are non-empty. Put the rule in a pure
   `.logic.ts` function (`isLoginFormValid`); the component only reflects it. Server-side validation
   is unaffected — this is a UX guard.
2. Remove / hide the "Forgot password?" element from `LoginPage.vue`. (Reintroduce when the
   password-reset story is built.)

## Key Files

- `frontend/src/features/auth/components/LoginPage.vue`
- `frontend/src/features/auth/logic/` (new `isLoginFormValid` logic + test)
- `frontend/acceptance/tests/frontend/login/login-page.spec.ts` (E2E: Sign In disabled when
  empty/partial; "Forgot password" element absent)

## Reproduction

1. Open the login page.
2. Click **Sign In** with both fields empty → the request is attempted (should be blocked).
3. Fill **only** the username, click Sign In → the request is attempted (should be blocked).
4. "Forgot password?" text is visible but non-clickable (dead placeholder).
