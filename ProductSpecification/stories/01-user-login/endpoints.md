# User Login - API Endpoints

## Existing (already implemented)

| Method | Path | Description |
|--------|------|-------------|
| POST | /api/auth/login | Authenticate user, create session (JSESSIONID cookie) |
| GET | /api/auth/csrf | Get CSRF token (XSRF-TOKEN cookie) |

## New Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/auth/activate | Validate activation token, return user info |
| POST | /api/auth/activate | Set password and activate account |
| GET | /api/auth/me | Get current authenticated user info |
| POST | /api/auth/logout | Invalidate session, clear cookie |

## Notes

- Endpoint authorization is an allow-list (deny-by-default): only `GET /api/auth/csrf`, `POST /api/auth/login`, and `GET`/`POST /api/auth/activate` are public. Every other endpoint under `/api/**` — including `/api/auth/me`, `/api/auth/logout`, and any newly added endpoint — requires authentication automatically
- Session-based auth via JSESSIONID cookie; CSRF token required for POST endpoints (except /api/auth/login which runs before CSRF is established)
- Activation token is a JWT passed as query parameter; no Authorization header needed
