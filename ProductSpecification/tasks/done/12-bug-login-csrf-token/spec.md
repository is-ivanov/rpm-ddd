# Task 12: Login CSRF Token

Type: bug
Issue: #129

## Problem

On prod (`https://rpm-ddd.onrender.com/login`), `POST /api/auth/login` returns **403
`INVALID_CSRF_TOKEN`** and the user cannot log in. `login.api.ts` sends the request without a
CSRF token, but the backend (Spring Security cookie-based CSRF, `CsrfConfigurer::spa`) requires
one. `activation.api.ts` already performs the handshake; login does not.

Confirmed live: `GET /api/auth/csrf` → 200 sets the `XSRF-TOKEN` cookie; retrying login **with**
the `X-XSRF-TOKEN` header returns a normal **401** (`Account not activated`). So the missing token
is the only blocker — the backend is fine.

Never caught because all login E2E/unit tests mock `/api/auth/login`. Latent bug unmasked by Task
11 (#127), now that the request actually reaches the backend.

## Solution

Frontend only (Variant A). `login()` primes `GET /api/auth/csrf`, then POSTs with
`X-XSRF-TOKEN: readCookie('XSRF-TOKEN')`, mirroring `activateAccount()`. Extract a shared CSRF
helper (`logic/csrf.ts`) and reuse it in both `login.api.ts` and `activation.api.ts` (dedupe the
`readCookie` + handshake currently local to activation). Update the login E2E backend Statements to
stub `/api/auth/csrf` so existing login Playwright tests stay green after the new GET call is added.

Backend is unchanged (the non-ProblemDetail 403 body is tracked separately as #130).

## Key Files

- `frontend/src/features/auth/logic/login.api.ts`
- `frontend/src/features/auth/logic/activation.api.ts` (dedupe into shared helper)
- `frontend/src/features/auth/logic/csrf.ts` (new shared helper)
- `frontend/src/features/auth/__tests__/login.api.test.ts` (red-frontend-api)
- `frontend/acceptance/tests/statements/backend/auth-backend.statements.ts` (stub `/api/auth/csrf`)

## Reproduction

1. Open `https://rpm-ddd.onrender.com/login`.
2. Enter any username and password, click **Sign In**.
3. DevTools → Network: `POST /api/auth/login` → 403 `{"code":"INVALID_CSRF_TOKEN", ...}`; no error
   is shown to the user.
