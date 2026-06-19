# Home Page - Notes & Considerations

## Warnings

### Functional Warnings
- Auth state is determined by GET /api/auth/me: 200 → authenticated, 401 → Welcome. The 401 is an expected, non-error outcome here — do not surface it as a failure banner.
- Post-login redirect must land on `/`. Today `login.api.ts` only returns void and the router has no redirect — wiring the redirect is part of this story.
- Logout must clear client-side user state immediately after POST /api/auth/logout succeeds, then show the Welcome screen — don't leave a stale avatar/menu rendered.
- Deep-linking: an unauthenticated user hitting `/` directly must get the Welcome screen, not a flash of the dashboard shell.

### UI/UX Warnings
- Avoid a dashboard/welcome flicker while /me is in flight — render a neutral loading state until the call resolves.
- Avatar initials must handle short or single-character names gracefully (e.g. first+last initial).
- The empty sidebar should read as intentional structure, not a broken/empty panel — keep it visually deliberate.
- Don't copy mockup placeholder names/emails into the component — user identity comes from the /me response.

### Technical Warnings
- `HomePage.vue` is currently a static stub (`RPM` + subtitle). This story replaces it; preserve the `data-testid` testing conventions.
- Roles are returned by /me but are an empty list today and unused here — do not branch layout on role yet (Story 4 introduces roles).
- Logout is a state-changing POST and requires the CSRF token (XSRF-TOKEN), same pattern as login.

---

## Suggestions & Future Enhancements

### Functional Suggestions
- A router navigation guard that protects future authenticated routes (defer until there are protected routes — Stories 4+).
- Persist/refresh current-user state in a shared store so other features can read it without re-calling /me.

### UI/UX Suggestions
- Populate the sidebar with navigation entries as Stories 4–11 land (Patients, Orders, Devices, Reports…).
- Replace the placeholder main area with real dashboard widgets (patient count, active alerts, pending orders) once that data exists.
- Profile / account-settings entry in the avatar menu (out of scope — no profile page yet).

### Technical Suggestions
- Extract a reusable `AppShell` layout (topbar + sidebar + content slot) so feature pages render inside it without duplicating chrome.
- Centralize the current-user fetch as a composable (e.g. `useCurrentUser`) backing both the shell and future features.

---

## Technical Notes

### Load Considerations
- Negligible. One GET /api/auth/me per page load. No data aggregation, no per-row queries.

### Security Considerations
- Authentication state is server-authoritative via the session cookie; the frontend only reflects it. Never infer "logged in" from client-side flags alone.
- Logout invalidates the HttpSession server-side (existing SecurityContextLogoutHandler) — frontend just triggers it and resets local state.
- Same allow-list/deny-by-default authorization as Story 1; /me requires authentication and returns 401 when unauthenticated, which the Welcome state relies on.

### Infrastructure Notes
- No new infrastructure. Reuses existing auth endpoints and the SPA-serving setup.
- Multi-instance safe: no client-side state that must be consistent across instances; auth state lives in the session.

### Integration Notes
- No external API dependencies.
- Reuses existing endpoints only: GET /api/auth/me (CurrentUserResponse: userId, login, email, firstName, lastName, status, roles) and POST /api/auth/logout.

---

## Additional Context

- No `interview.md` for this story — scope confirmed directly with the user: dashboard shell when authenticated, welcome screen with Log in button when not, same for all roles, sidebar and dashboard body are placeholders for now.
- Existing assets to build on: `frontend/src/features/home/components/HomePage.vue` (stub to replace), `frontend/src/router/index.ts` (`/` → home, `/login` → login), auth feature under `frontend/src/features/auth/` (login/logout/csrf clients to extend with a `me` client and logout wiring).
- Backend already provides everything needed — this story adds no `src/main/java` code.
