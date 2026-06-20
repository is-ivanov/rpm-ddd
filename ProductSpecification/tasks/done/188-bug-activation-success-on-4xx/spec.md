# Task 188: Activation Page Shows Success on Backend 4xx

Type: bug
Issue: #188

## Problem

`ActivationPage.submitActivation()` calls `activateAccount()` then sets `activated.value = true`
**unconditionally**. `activation.api.ts` `activateAccount()` awaits `postJsonWithCsrf()` and
**discards the Response** — it never checks `response.ok`. `csrf.ts` `postJsonWithCsrf` returns the
raw `Response` without any status check. So on any 4xx (weak password, expired token, validation
error) the user still sees the "Account Activated!" screen — a fake success.

There is also **no try/catch** on submit (`loadAccount` has one, `submitActivation` does not), so a
network error / rejection becomes an unhandled promise rejection and the form freezes with no
feedback.

Not covered by any spec (main or extended). Showing success on failure is broken behaviour → bug,
not an improvement.

## Solution

Make the POST path **throw on non-2xx**: parse the RFC-9457 ProblemDetail (exactly as
`validateActivationToken` already does for the GET path) and raise an `ActivationError`. Wrap
`submitActivation` in try/catch and render the server error (weak password / expired token) instead
of the success screen. Keep the success transition only on a 2xx response.

Pairs with #190 step 1 (type-checked ESLint) — `no-floating-promises` would have caught the floating
submit; and with #190 step 2 (runtime response validation) which generalises the ok-check.

## Key Files

- `frontend/src/features/auth/components/ActivationPage.vue` (try/catch, error state, gate success on 2xx)
- `frontend/src/features/auth/logic/activation.api.ts` (`activateAccount` throws on non-2xx)
- `frontend/src/features/auth/logic/csrf.ts` (or a shared `apiFetch` ok-check)
- `frontend/acceptance/tests/frontend/...` (E2E: 4xx → server error shown, NOT success)

**All tests written in this task's TDD cycle MUST be tagged with issue #188** (per tech binding `tdd.md`).

## Reproduction

1. Open `/activate?token=<valid-token>`.
2. Enter a password the backend rejects (e.g. too weak) and submit → backend returns 4xx.
3. UI shows "Account Activated!" anyway (expected: an error message, no success).
4. Variant: drop the network mid-submit → unhandled rejection, form freezes.
