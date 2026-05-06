# User Login

## Brief Description
Users log in with login + password (ACTIVE status required). New users activate via a two-step email-token flow. Session-based auth with CSRF protection. First frontend story: login and activation pages.

## Flow
1. User submits credentials to POST /api/auth/login
2. System validates credentials and checks user status is ACTIVE → returns 200 with JSESSIONID cookie
3. Non-ACTIVE users receive 401 with status-specific message
4. Frontend calls GET /api/auth/me to display current user info
5. User clicks activation link from email (GET /api/auth/activate?token=XXX)
6. System validates JWT, returns user info (login, email) for password-set form
7. User submits new password (POST /api/auth/activate with {token, newPassword})
8. System validates JWT, applies PasswordPolicy, sets password hash, changes status to ACTIVE
9. User logs out via POST /api/auth/logout → session invalidated, JSESSIONID cleared

## Acceptance Criteria
- ACTIVE users log in successfully and receive a valid session
- PENDING, LOCKED, INACTIVE users receive 401 with descriptive message
- GET /api/auth/activate?token=XXX returns user info from JWT claims
- POST /api/auth/activate sets password and activates user (status → ACTIVE)
- Invalid/expired activation tokens return appropriate errors
- GET /api/auth/me returns current authenticated user info
- POST /api/auth/logout invalidates session and clears cookie
- Frontend login page with form validation and error display
- Frontend activation page with password complexity feedback

## Validation Rules
| Field | Rule |
|-------|------|
| Login | Required, non-empty |
| Password | Required, non-empty |
| Activation token | Valid JWT, not expired, type=activation |
| New password | 12-128 chars, upper, lower, digit, special, no whitespace |

## Screen States
- Login page: form (login + password), submit, error states (wrong credentials, inactive account)
- Activation page: password-set form with complexity feedback, success/error states
- Logged-in state: user info displayed via /me endpoint

## Core Requirements
- Session-based auth with JSESSIONID cookie (no JWT for sessions)
- CSRF protection via SPA cookie pattern (XSRF-TOKEN cookie)
- Two-step activation: GET returns info, POST sets password + activates
- PasswordPolicy enforced during activation only
- Server-side logout (invalidate HttpSession)
- Vue 3 + Tailwind frontend pages
