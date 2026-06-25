# User Management: Admin User Grid & Create User

## Brief Description
The **Admin Center → Users** screen: an authenticated admin views every user in a client-side grid (filter + sort on all columns) and creates new users via a modal. Create reuses the existing POST /api/admin/users flow (user → PENDING → activation email). One new backend endpoint, GET /api/admin/users, returns the full list with resolved actor names. The dashboard shell becomes a reusable layout with an "Admin Center" sidebar group and a collapse toggle.

## Flow
1. Authenticated user opens the dashboard → sidebar shows "Admin Center → Users"
2. Click "Users" → navigate to the Users page inside the shared shell
3. Page calls GET /api/admin/users → grid renders rows (name, login, email, status, audit fields)
4. User filters/sorts any column → grid updates client-side (no server round-trip)
5. Click "Register user" → modal opens with name/login/email/timezone fields (timezone defaults to the app default, Central Europe)
6. Submit → POST /api/admin/users → on success modal closes and grid refreshes
7. Duplicate login/email → 422 field error shown under the offending field
8. Top-bar collapse toggle hides/shows the sidebar; state persisted in localStorage

## Acceptance Criteria
- GET /api/admin/users returns every user with resolved `createdBy`/`updatedBy` names ("System" for the seed actor)
- Users grid shows columns: Full name, Login, Email, Status, Created, Created by, Updated, Updated by
- Client-side filtering and sorting work on every grid column
- "Register user" modal submits name/login/email/timezone; success refreshes the grid
- Duplicate login or email surfaces a 422 field-level error under that field
- Sidebar shows an "Admin Center" group with a "Users" sub-item
- Sidebar collapse toggle works and its state survives a reload (localStorage)
- Any authenticated user may reach the endpoint and the Admin Center (no role gate yet)

## Validation Rules
| Field | Rule |
|-------|------|
| firstName | required |
| middleName | optional, ≤255 |
| lastName | required |
| login | @NotBlank, ≤ Login.MAX_LENGTH, unique → 422 |
| email | @NotBlank @Email, ≤ EmailAddress.MAX_LENGTH, unique → 422 |
| timezone | required IANA zone id; defaults to the app default (Central Europe, `Europe/Berlin`) |

## Screen States
- Users grid: header + "Register user" button + sortable/filterable table; loading variant
- Register-user modal: form (name/login/email/timezone), per-field errors, loading submit button
- Sidebar expanded / collapsed (persisted)

## Grid Columns & Filters
| Column | Filter type | Notes |
|--------|-------------|-------|
| Full name | text "contains" | |
| Login | text "contains" | |
| Email | text "contains" | |
| Status | enum multi-select | center-aligned column |
| Created / Updated | date range (from – to) | relative time + full-on-hover |
| Created by / Updated by | text "contains" | abbreviated actor name |

- Sorting available on **every** column; filtering is client-side over the full list. (Sort/filter on the timestamp columns operate on the underlying absolute instant, not the relative label.)
- **Timestamps**: shown as **relative time** (e.g. `7 days ago`); hovering reveals a tooltip with the **full absolute** time — `YYYY-MM-DD HH:MM:SS` + TZ label (abbreviation when known, e.g. `CET`; else UTC offset, e.g. `UTC+05:00`) + IANA zone. Rendered in the **viewer's profile timezone** (`User.timeZone`), NOT the browser. Backend stores UTC instants.
- **Audit actor**: shown abbreviated as `J. Doe` (first-name initial + last name); full `First [Middle] Last` on hover (tooltip). Seed/SYSTEM actor = `System`.

## Core Requirements
- New endpoint GET /api/admin/users + list response DTO (rows carry resolved actor names, never raw UUIDs)
- Add `updatedAt`/`updatedBy` to the `User` aggregate (+ migration); init = created on creation, updated on activation (`updatedBy` = activating user)
- Add `timeZone` (IANA id, e.g. `Europe/Berlin`) to the `User` aggregate (+ migration). **App-default timezone = Central Europe (`Europe/Berlin`)**. Set at registration: the **Register-user form includes a timezone picker** (admin chooses the new user's zone; pre-filled with the app default). Expose `currentUser.timeZone` via `GET /api/auth/me`; the grid formats every timestamp in the viewer's zone. **A user changing their OWN timezone is out of scope this story** — deferred to a profile-settings story
- Actor name resolution via a list/read model — fetch everything upfront, no per-row lookups; the row carries the actor's **full** name (parts or composed) so the frontend renders both the `J. Doe` abbreviation and the full-name tooltip
- Create path: admin still never sets the password (PENDING + activation flow unchanged), but the request now carries `timeZone` — `RegisterUserRequest`/`RegisterUserCommand` and `User.register(...)` gain a timezone parameter
- Extract `DashboardShell` into a reusable layout wrapping nested routes
- API client validates the GET and create response shapes at the boundary (RFC 9457 for errors)
- Vue 3 + Tailwind; reuse `LoadingButton`, problem-detail schema, `current-user` plumbing
