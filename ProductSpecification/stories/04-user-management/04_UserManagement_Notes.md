# User Management: Admin User Grid & Create User - Notes & Considerations

## Warnings

### Functional Warnings
- This story is **slot #4**, replacing the old "User Management: roles, agencies, permissions" epic. Roles, agencies and permissions are explicitly NOT here — do not introduce a `Role`/`Agency` concept.
- The create flow is unchanged: admin creates → user is `PENDING` with a placeholder (random) password → `UserRegisteredEvent` → activation email (Story 2) → user sets their own password and becomes `ACTIVE`. The admin does NOT set the password.
- Audit fields: on creation, `updatedAt = createdAt` and `updatedBy = createdBy`. `createdAt` reuses the existing `registeredAt`; `createdBy` already exists as a `UserId`. Activation (password set + status → ACTIVE) updates `updatedAt`/`updatedBy`, with `updatedBy` = the activating user (self-service).
- The grid must never show raw UUIDs — `createdBy`/`updatedBy` must be resolved to person ФИО. The seed/`SYSTEM` actor (`00000000-…`) is shown as the pseudonym "System".
- Duplicate handling = field-level errors: `LoginAlreadyExistsException`/`EmailAlreadyExistsException` → RFC 9457 ProblemDetail with `fieldErrors` → 422, shown under login/email (same pattern as the login story).
- Status set is PENDING / ACTIVE / LOCKED / INACTIVE — **display-only** this story. No status transitions exposed (no edit, lock, deactivate, delete).

### UI/UX Warnings
- Don't copy mockup placeholder names/emails into the grid or modal — user data comes from the API response only.
- The "Create" modal/drawer opens from a button above the grid; on success it closes and the grid refreshes — don't leave a stale list.
- Submit button needs a loading/disabled state during the POST (async-action button rule); disable form fields in-flight.
- Collapsed/expanded sidebar must read as intentional, and must restore from `localStorage` without a flicker on load.
- Filtering/sorting applies to every visible column, including Status and the resolved actor names.

### Technical Warnings
- Avoid per-row actor lookups (N+1). Design the GET as a list/read model so one query returns the grid rows with names already joined ("fetch everything upfront").
- Extracting `DashboardShell` into a layout touches `HomePage` (Story 3) — keep the home dashboard rendering inside the same shell; preserve existing `data-testid`s.
- Auth: `/api/admin/**` already requires authentication via the allow-list policy. No new security rule needed; no role restriction yet (deferred to the roles story).
- Migration required for `updatedAt`/`updatedBy` columns; backfill existing rows = their created values.
- localStorage is per-browser/per-device — collapse state is intentionally not synced server-side.
- **Timezone display (DECISION).** Store UTC instants; render in the **viewer's profile timezone** — a new `User.timeZone` (IANA id) field. Grid shows **relative** time (`7 дней назад`) with a hover tooltip carrying the full absolute time in that zone (`YYYY-MM-DD HH:MM:SS МСК · UTC+03:00 · Europe/Moscow`). This is the GitHub/GitLab pattern (relative primary + absolute-on-hover); chosen over the actor's timezone because a per-actor column is unscannable/unsortable. Use the IANA tz database (not fixed offsets) so DST is handled. Sort/filter must use the underlying instant, not the relative string.
- **`User.timeZone` scope (CONFIRM).** This story adds the field + migration (default `Europe/Moscow`) and exposes `currentUser.timeZone` via `/api/auth/me`; it does **not** add a TZ-picker UI — editing one's timezone is deferred to a profile-settings story. If a default-only timezone is unacceptable for first release, raise before implementation.

---

## Suggestions & Future Enhancements

### Functional Suggestions
- Edit user, lock/deactivate (status transitions), delete user — future stories.
- User detail page / row actions — deferred (grid is view + "Create" only).
- Roles, agencies, permissions, and restricting Admin Center to an admin role — the roles story.
- Password reset / forgot password — out of scope.

### UI/UX Suggestions
- Server-side pagination/search once the user table grows large (deferred; acceptable client-side at tens of users).
- Bulk operations on the grid — future.
- Column visibility / saved filters — future.

### Technical Suggestions
- The reusable layout extracted here (`DashboardShell` → nested-route layout) should serve every later Admin Center / feature page without duplicating chrome.
- Consider a shared current-user/store read so feature pages don't re-call `/me`.

---

## Technical Notes

### Load Considerations
- Admin/staff user count is small (tens). Returning the full list and filtering/sorting client-side is acceptable. Revisit with server-side pagination only if the user table grows large (explicitly deferred). Unlike patients, admins are few.

### Security Considerations
- Any authenticated user may reach `/api/admin/users` and the Admin Center this story — no role gate (roles don't exist yet). The allow-list policy denies unauthenticated access by default.
- Create input validated as today (firstName/lastName required, login/email NotBlank + length + uniqueness). Mass-assignment guarded by the existing `RegisterUserRequest` fields only.
- All error responses use RFC 9457 ProblemDetail (validation → 422 with `fieldErrors`).

### Infrastructure Notes
- DB migration for the two new `User` audit columns. No new external services.

### Integration Notes
- Relies on Story 2 (real activation email delivery) and Story 3 (dashboard shell). The create-user pipeline (event → JWT → activation email) already works end-to-end and is reused as-is.

---

## Additional Context

- See `interview.md` for the full scope/out-of-scope breakdown, the API status table, the per-decision rationale (audit fields, actor resolution, client-side grid, modal create, duplicate handling, layout extraction, collapse persistence), and the "Already Implemented (REUSE)" / "NOT Yet Implemented (Gaps)" inventories that drive the backend and frontend work.
