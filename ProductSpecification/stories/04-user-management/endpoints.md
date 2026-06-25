# User Management: Admin User Grid & Create User - API Endpoints

## Existing (reused / modified)

| Method | Path | Change | Description |
|--------|------|--------|-------------|
| POST | /api/admin/users | MODIFIED | Create user (PENDING + activation email). Request now carries `timeZone`. |
| GET | /api/auth/me | MODIFIED | Current-user info; response now carries `timeZone` (viewer's profile zone). |

## New Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/admin/users | Full user list for the admin grid (rows carry resolved actor names, never raw UUIDs). |

## Notes

- **Auth:** any authenticated user may reach `/api/admin/**` (allow-list policy already requires auth by default; no role gate this story).
- **Grid is client-side:** GET returns the full list (no paging/search params); the frontend filters/sorts every column.
- **Default order (deterministic):** the backend returns rows ordered `createdAt DESC, userId DESC` — newest first, with the UUIDv7 `userId` as a tiebreaker that guarantees a total order even when two `createdAt` instants collide. This is the order shown before the user clicks a sort header, and what acceptance tests assert.
- **Status order (lifecycle, not alphabetical):** the canonical status order is `PENDING → ACTIVE → LOCKED → INACTIVE` (the enum ordinal). The Status filter lists its multi-select options in this order, and sorting the Status column ranks by this lifecycle order — never alphabetically.
- **Actor resolution:** `createdBy`/`updatedBy` UserIds resolve to person name parts (first/middle/last) in one read-model query — no per-row lookups. The seed/`SYSTEM` actor renders as `System` (returned with `firstName: "System"`, empty `lastName`); the frontend composes the `J. Doe` abbreviation + full `First [Middle] Last` tooltip from the parts.
- **Timestamps:** `createdAt`/`updatedAt` are UTC instants (ISO 8601). Sort/filter operate on the instant; the relative label + absolute-on-hover tooltip are rendered client-side in the viewer's `timeZone` (from `/api/auth/me`), not the browser zone.
- **timeZone:** IANA zone id (e.g. `Europe/Berlin`). On create the admin picks the new user's zone (pre-filled with the app default, Central Europe). The grid row does NOT carry a per-user zone — every timestamp renders in the viewer's zone.
- **Duplicate login/email** → RFC 9457 ProblemDetail (`status: 422`) with `fieldErrors` keyed to `login`/`email`.
