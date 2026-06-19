# Story 3: Home page — Progress

## Spec
- [S] interview (scope confirmed directly with the user during `/story` — dashboard shell when authenticated, welcome screen with a Log in button when not, same layout for all roles, sidebar + dashboard body are placeholders; no `interview.md`)
- [x] story
- [x] mockups
- [S] api-spec (no new HTTP endpoints — Home page is frontend-only and consumes the existing GET /api/auth/me + POST /api/auth/logout, both already documented in Story 1's `endpoints.md` with auth/CSRF notes; per api-spec MVP "when in doubt, leave it out". Mirrors Story 2's `[S] api-spec`.)
- [~] test-spec

<!-- Scenario sections (Frontend / Backend / Security / …) are appended when test-spec is generated — they are derived from tests/*.md. This is a frontend-only story: no new backend endpoints (GET /api/auth/me and POST /api/auth/logout already exist from Story 1). -->
