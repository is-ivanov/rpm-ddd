# Infrastructure Tests — Home Page

No infrastructure test scenarios — Home page introduces no persistence, scheduled jobs, or external-service dependencies. It consumes existing auth endpoints (`GET /api/auth/me`, `POST /api/auth/logout`) whose failure and recovery behaviour is covered by Story 1.
