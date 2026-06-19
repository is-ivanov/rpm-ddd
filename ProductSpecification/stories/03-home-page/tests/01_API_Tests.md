# API Tests — Home Page

No API test scenarios — Home page is frontend-only. It introduces no new HTTP endpoints; it consumes the existing `GET /api/auth/me` (current user) and `POST /api/auth/logout`, both implemented and already covered by Story 1's API and security tests. The post-login redirect and the authenticated/unauthenticated rendering are frontend concerns, verified in `02_UI_Tests.md`.
