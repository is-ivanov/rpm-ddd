# Task 11: Prod API Base URL

Type: bug

## Problem

On the deployed Render service — a single Spring Boot image that serves the SPA **and** `/api/*` from the same origin — the login page (and every auth API call) sends requests to `http://localhost:8080/api/...` instead of the same-origin backend. The request fails in the browser; nothing happens for the user — no error is shown, only a console error.

Two defects:

1. **Hard-baked localhost base URL.** `frontend/vite.config.ts` resolves `const apiUrl = process.env.VITE_API_URL || 'http://localhost:8080'` and bakes it into the bundle via `define: { 'import.meta.env.VITE_API_URL': JSON.stringify(apiUrl) }`. On the production build `VITE_API_URL` is unset (absent from `.github/workflows/deploy.yml`), so the literal `http://localhost:8080` is compiled into the client. `login.api.ts`'s `?? ''` fallback never fires because the baked value is a non-empty string. `http://localhost:8080` is only valid as the dev *proxy target*, never as the client's base URL.

2. **Silent failure.** `LoginPage.submitLogin` only catches `LoginError`. A rejected `fetch` (network/CORS/unexpected error) throws a `TypeError`, which is swallowed — the user gets no feedback.

## Solution

1. In `vite.config.ts`, split the two concerns: the **client base URL** baked into the bundle defaults to `''` (relative → same-origin); `http://localhost:8080` remains only as the **dev proxy target** (`server.proxy`). Production then issues relative `/api/...` requests against the backend that serves the SPA.
2. In `LoginPage.vue`, handle unexpected (non-`LoginError`) failures by showing a generic error banner. Extract the error→view mapping into testable logic.

## Key Files

- `frontend/vite.config.ts`
- `frontend/src/features/auth/components/LoginPage.vue`
- `frontend/src/features/auth/logic/` (new error→view mapping logic + test)

## Reproduction

1. Deploy `main` to Render (single web service, backend image serving the SPA).
2. Open the app, navigate to `/login`.
3. Enter any username/password, click **Sign In**.
4. DevTools → Network: the request goes to `http://localhost:8080/api/auth/login` and fails. Console shows an error. The UI shows nothing to the user.
