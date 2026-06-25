# Story 4: User management — admin user grid & create user — Interview

> This is the **first** story of what was originally the "User Management" epic (#4 in the
> old backlog: *roles, agencies, basic permissions*). DECISION: the epic and the rest of the
> old backlog (stories 5–11) are dropped — we add stories as needed. This story takes slot #4.
> Roles, agencies and permissions are **not** in this story; they come in later stories when
> needed.

## Scope

**In scope:**
- **GET /api/admin/users** — list all users for the admin grid (full list, no server-side paging).
- Reuse the existing **POST /api/admin/users** create-user flow (no backend changes to the
  create path beyond what the grid needs).
- Frontend **Admin Center → Users** navigation entry in the dashboard sidebar.
- Frontend **users grid**: columns Name (ФИО), Login, Email, Status, audit fields
  (createdAt, createdBy, updatedAt, updatedBy). Client-side **filtering + sorting on every
  grid column**.
- Frontend **"Create user" modal/drawer** over the grid (name, login, email), with
  field-level validation errors.
- **Sidebar collapse**: a toggle button in the top bar collapses/expands the left sidebar
  (GitLab/Bitbucket style); collapsed/expanded state persisted in `localStorage`.

**Out of scope (future stories):**
- Edit user, lock/deactivate (status change), delete user.
- User detail page / row actions (the grid is view + "Create" only).
- Roles, agencies, permissions (no `Role`/`Agency` concept introduced here).
- Server-side pagination / search (grid is client-side over the full list).
- Bulk operations.
- Password reset / forgot password.

## API Endpoints Used (with implementation status)

| Method | Path | Status | Notes |
|--------|------|--------|-------|
| POST | /api/admin/users | ALREADY IMPLEMENTED | Creates user (PENDING, placeholder password), publishes `UserRegisteredEvent` → activation email. Reused as-is. |
| GET | /api/admin/users | NOT YET IMPLEMENTED | Returns the full user list for the grid (each row: name, login, email, status, audit fields with actor names). |

**Auth:** any authenticated user may reach the admin endpoints and the Admin Center.
DECISION: no role restriction yet — roles don't exist. Restricting Admin Center to an admin
role is deferred to the roles story. The existing allow-list security policy already requires
authentication for `/api/admin/**` by default.

## Key Architectural Decisions

- DECISION: This story = slot **#4**. The old "User Management" epic and old backlog stories
  5–11 are deleted from `stories.md`; new stories are added on demand.
- DECISION: **No roles / no agencies** in this story. The create-user form keeps the existing
  fields only: firstName, middleName (optional), lastName, login, email.
- DECISION: **Create flow is unchanged** — admin creates a user → user is `PENDING` with a
  placeholder password → `UserRegisteredEvent` → activation email (Story 2) → the user sets
  their own password and becomes `ACTIVE`. The admin does NOT set the password.
- DECISION: **Audit fields.** Add `updatedAt` and `updatedBy` to the `User` aggregate now
  (they don't exist yet). On creation, initialize `updatedAt = createdAt` and
  `updatedBy = createdBy`. The activation step (password set + status → ACTIVE) updates them,
  with `updatedBy` = the activating user themselves (self-service). `createdAt` reuses the
  existing `registeredAt`; `createdBy` already exists as a `UserId` association.
- DECISION: **Actor display in the grid** = the actor's full name (ФИО), resolved from the
  `UserId`. The seed/`SYSTEM` actor (`00000000-…`) is shown as a pseudonym like **"System"**.
  ACTION: the GET endpoint must resolve `createdBy`/`updatedBy` UserIds to person names
  (join/lookup) — the grid never shows raw UUIDs.
- DECISION: **Grid is client-side.** GET returns the full list; the frontend does all
  filtering and sorting (over every column). Acceptable because admin/staff users are few
  (tens), unlike patients. Server-side paging is deferred.
- DECISION: **Create form = modal/drawer** opened from a "Create" button above the grid; on
  success the grid refreshes.
- DECISION: **Duplicate handling** = field-level errors. `LoginAlreadyExistsException` /
  `EmailAlreadyExistsException` → RFC 9457 ProblemDetail with `fieldErrors` → shown under the
  login/email field (same pattern as the login story). Status 422.
- DECISION: **Sidebar shell becomes a reusable layout.** Today `DashboardShell` (topbar +
  sidebar + main) lives only inside `HomePage`. ACTION: extract it into a layout that wraps
  nested routes so the Users page renders inside the same topbar + sidebar. Add an
  "Admin Center" nav **group** in the sidebar with a **"Users"** sub-item.
- DECISION: **Sidebar collapse state persisted in `localStorage`** so it survives reloads.

## Business Rules & Constraints

- New users are created `PENDING` with a placeholder (random) password; they cannot log in
  until they activate via the email link and set a password.
- User status set: PENDING / ACTIVE / LOCKED / INACTIVE (display-only in the grid this story;
  no status transitions exposed in the UI).
- Create validation (existing): firstName/lastName required, middleName optional (≤255),
  login `@NotBlank` ≤ `Login.MAX_LENGTH`, email `@NotBlank @Email` ≤ `EmailAddress.MAX_LENGTH`.
- Login and email must be unique → duplicate → 422 field error.
- Grid filtering/sorting applies to every visible column, including status and the
  resolved actor names.

## Already Implemented (REUSE)

- **UserResource** — `POST /api/admin/users` (`@RequestMapping("/api/admin/users")`), uses
  `SecurityCurrentActorProvider.currentUserId()` as `createdBy`.
- **UserRegistrationService.registerUser(command, createdBy)** — creates the user, publishes
  the event.
- **RegisterUserRequest / RegisterUserCommand** — name/login/email fields and `toCommand()`.
- **User domain** — `User` aggregate (`register(...)`, `activate(...)`), `PersonName`,
  `EmailAddress`, `Login`, `Password`, `UserStatus`, `UserId`, `registeredAt`, `createdBy`,
  `UserRepository`.
- **Activation pipeline** — `UserRegisteredEventListener` → JWT → email (Story 2 real
  delivery). Create-user reuses this end-to-end.
- **Security** — allow-list policy; `/api/admin/**` already requires authentication;
  `SecurityCurrentActorProvider`, `SystemActors.SYSTEM_USER_ID`.
- **Frontend shell** — `DashboardShell.vue` (topbar + empty sidebar placeholder + main),
  `DashboardTopBar.vue` (logo + `UserMenu`), `HomePage.vue`, `auth.store`, router, the
  RFC 9457 problem-detail schema and `LoadingButton`, `current-user` plumbing.

## NOT Yet Implemented (Gaps)

**Backend**
- `GET /api/admin/users` endpoint + list response DTO (rows with resolved actor names).
- `User` audit fields `updatedAt` / `updatedBy` (+ DB migration); initialize = created on
  creation; update on activation.
- Resolving `createdBy`/`updatedBy` UserId → person name (with "System" pseudonym for the
  seed actor) — likely a list/read model so one query returns the grid rows (avoid
  per-row lookups; "fetch everything upfront").

**Frontend**
- Extract `DashboardShell` into a reusable layout wrapping nested routes.
- Sidebar navigation: "Admin Center" group + "Users" sub-item.
- Top-bar sidebar **collapse toggle**, persisted in `localStorage`.
- New route + Users page: grid component with client-side filter/sort over all columns.
- "Create user" modal/drawer: form (name/login/email), submit → POST, field-level errors,
  loading state on the submit button, refresh grid on success.
- API client for `GET /api/admin/users` (+ response schema validation at the boundary) and
  the create call.

## Cross-Story Dependencies

- **Story 2 (Email integration)** — provides real activation email delivery; the create-user
  flow relies on it but already works end-to-end.
- **Story 3 (Home page)** — provides the dashboard shell (topbar + sidebar placeholder) this
  story extends; the empty sidebar was explicitly a placeholder "for Stories 4–11 navigation".
- **Later user-management stories** (roles, agencies, edit/lock, permissions) build on the
  grid and create flow introduced here — kept out of scope deliberately.

## Testing Considerations

- Acceptance (Level 1): one happy-path scenario per endpoint behavior — list users returns
  the grid rows with resolved actor names; create-user happy path is already covered (extend
  the existing acceptance test only if a new observable consequence is added).
- The `createdBy`/`updatedBy` name resolution and the "System" pseudonym are deterministic →
  assert exact values.
- Frontend: client-side filter/sort is pure logic → unit-test the filtering/sorting functions;
  the grid render and the collapse toggle are presentational (align-design / Playwright).
- Sidebar collapse persistence (`localStorage`) is a small piece of presentational state.

## Performance / Rate Limits

- Admin/staff user count is small (tens), so returning the full list and filtering/sorting
  client-side is acceptable. If the user table ever grows large, revisit with server-side
  pagination (explicitly deferred).
