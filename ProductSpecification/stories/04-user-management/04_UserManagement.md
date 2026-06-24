# User Management: Admin User Grid & Create User

## Brief Description
The **Admin Center → Users** screen: an authenticated admin views every user in a client-side grid (filter + sort on all columns) and creates new users via a modal. Create reuses the existing POST /api/admin/users flow (user → PENDING → activation email). One new backend endpoint, GET /api/admin/users, returns the full list with resolved actor names. The dashboard shell becomes a reusable layout with an "Admin Center" sidebar group and a collapse toggle.

## Flow
1. Authenticated user opens the dashboard → sidebar shows "Admin Center → Users"
2. Click "Users" → navigate to the Users page inside the shared shell
3. Page calls GET /api/admin/users → grid renders rows (name, login, email, status, audit fields)
4. User filters/sorts any column → grid updates client-side (no server round-trip)
5. Click "Create" → modal opens with name/login/email fields
6. Submit → POST /api/admin/users → on success modal closes and grid refreshes
7. Duplicate login/email → 422 field error shown under the offending field
8. Top-bar collapse toggle hides/shows the sidebar; state persisted in localStorage

## Acceptance Criteria
- GET /api/admin/users returns every user with resolved `createdBy`/`updatedBy` names ("System" for the seed actor)
- Users grid shows columns: Name (ФИО), Login, Email, Status, createdAt, createdBy, updatedAt, updatedBy
- Client-side filtering and sorting work on every grid column
- "Create user" modal submits name/login/email; success refreshes the grid
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

## Screen States
- Users grid: header + "Create" button + sortable/filterable table; loading variant
- Create modal: form (name/login/email), per-field errors, loading submit button
- Sidebar expanded / collapsed (persisted)

## Core Requirements
- New endpoint GET /api/admin/users + list response DTO (rows carry resolved actor names, never raw UUIDs)
- Add `updatedAt`/`updatedBy` to the `User` aggregate (+ migration); init = created on creation, updated on activation (`updatedBy` = activating user)
- Actor name resolution via a list/read model — fetch everything upfront, no per-row lookups
- Create path unchanged — admin never sets the password
- Extract `DashboardShell` into a reusable layout wrapping nested routes
- API client validates the GET and create response shapes at the boundary (RFC 9457 for errors)
- Vue 3 + Tailwind; reuse `LoadingButton`, problem-detail schema, `current-user` plumbing
