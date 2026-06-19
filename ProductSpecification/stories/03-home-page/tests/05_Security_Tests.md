# Security Tests — Home Page

No story-specific security test scenarios — Home page introduces no new endpoints and accepts no new user input.

- **Authorization** — the dashboard must not expose authenticated data to anonymous visitors. This is enforced server-side: `GET /api/auth/me` and `POST /api/auth/logout` require authentication under the deny-by-default allow-list and return `401` when unauthenticated. Covered by Story 1's security tests. The frontend merely reflects that `401` by rendering the welcome screen (verified in `02_UI_Tests.md` §1).
- **CSRF** — the only state-changing request, `POST /api/auth/logout`, is protected by the global SPA CSRF mechanism (XSRF-TOKEN), covered by Story 1.
- **XSS** — the only user-provided text rendered is the current user's own name/email from `GET /api/auth/me`. It is mitigated by the framework's auto-escaping text interpolation, and the value is self-scoped (a user only ever sees their own profile), so there is no cross-user injection surface. A defence-in-depth check is listed in `extended/05_Security_Tests_Extended.md`.
