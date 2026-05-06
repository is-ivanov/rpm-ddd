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

- All auth endpoints are publicly accessible (no authentication required) except /api/auth/me and /api/auth/logout
- Session-based auth via JSESSIONID cookie; CSRF token required for POST endpoints (except /api/auth/login which runs before CSRF is established)
- Activation token is a JWT passed as query parameter; no Authorization header needed
