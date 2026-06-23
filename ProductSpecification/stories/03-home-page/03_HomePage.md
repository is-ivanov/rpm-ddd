# Home Page

## Brief Description
The application landing page at `/`. Unauthenticated visitors see a welcome screen with a Log in button; authenticated users see the dashboard shell (topbar + empty sidebar + main area). The dashboard body is a placeholder for now — feature modules arrive in Stories 4–11. Same layout for all roles. Frontend-only: no new backend endpoints.

## Flow
1. Visitor opens `/` → frontend calls GET /api/auth/me
2. 401 (unauthenticated) → render Welcome screen (logo + welcome text + "Log in" button)
3. Click "Log in" → navigate to the login page
4. After successful login → redirect to `/` (dashboard)
5. 200 (authenticated) → render Dashboard shell from the /me response
6. Topbar shows logo (left), user avatar (initials) + dropdown menu (right)
7. Open avatar menu → user name/email and "Log out"
8. Click "Log out" → POST /api/auth/logout → render Welcome screen

## Acceptance Criteria
- Unauthenticated `/` shows the Welcome screen with logo and a Log in button
- Log in button navigates to the login page
- Successful login redirects to `/` and shows the Dashboard shell
- Dashboard topbar shows the logo and the authenticated user's avatar (initials)
- Avatar dropdown shows the user's name/email and a Log out action
- Log out invalidates the session and returns to the Welcome screen
- Left sidebar renders as an empty placeholder panel
- Main content area shows placeholder dashboard text
- Same layout and content for every role

## Validation Rules
| Field | Rule |
|-------|------|
| — | No user input on this story (display + navigation only) |

## Screen States
- Welcome (unauthenticated): logo, welcome heading/text, Log in button
- Dashboard (authenticated): topbar (logo + avatar/menu), empty left sidebar, main area with placeholder text
- Avatar menu open: user name/email + Log out
- Loading: transient state while GET /api/auth/me resolves

## Core Requirements
- Auth state derived from GET /api/auth/me (200 → authenticated, 401 → Welcome)
- No new backend endpoints — reuse existing GET /api/auth/me and POST /api/auth/logout
- Post-login redirect to `/`
- Avatar = initials from firstName + lastName (no image upload)
- Empty sidebar is an intentional placeholder for Stories 4–11 navigation
- Vue 3 + Tailwind; replaces the current static `HomePage.vue` stub
