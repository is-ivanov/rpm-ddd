interface ActorName {
  readonly firstName: string;
  readonly middleName: string | null;
  readonly lastName: string;
}

interface AdminUserAudit {
  readonly createdAt: string;
  readonly createdBy: ActorName;
  readonly updatedAt: string;
  readonly updatedBy: ActorName;
}

export interface AdminUser {
  readonly userId: string;
  readonly name: ActorName;
  readonly login: string;
  readonly email: string;
  readonly status: string;
  readonly audit: AdminUserAudit;
}

export interface ExpectedUserRow {
  readonly name: string;
  readonly login: string;
  readonly email: string;
  readonly status: string;
  readonly createdBy: string;
  readonly updatedBy: string;
}

export const SEED_ACTOR_DISPLAY = 'System';

export const JOHN_DOE: ActorName = { firstName: 'John', middleName: 'Robert', lastName: 'Doe' };
const SARAH_CONNOR: ActorName = { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' };
const SYSTEM_ACTOR: ActorName = { firstName: 'System', middleName: null, lastName: '' };

// Deterministic admin user list returned by GET /api/admin/users.
// Ordered newest-first (audit.createdAt DESC) to match the backend's deterministic order.
// Covers all four statuses, normal audit actors (-> abbreviated "J. Doe" / "S. Connor"),
// and the seed actor (empty last name -> rendered verbatim as "System").
// Timestamps are non-round (minutes/seconds/millis) per the test-data realism rule.
export const SEVERAL_ADMIN_USERS: readonly AdminUser[] = [
  {
    userId: '00000000-0000-0000-0000-000000000001',
    name: { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' },
    login: 's.connor',
    email: 's.connor@rpm.local',
    status: 'ACTIVE',
    audit: {
      createdAt: '2026-06-22T14:30:51.217Z',
      createdBy: JOHN_DOE,
      updatedAt: '2026-06-24T08:11:42.905Z',
      updatedBy: SARAH_CONNOR,
    },
  },
  {
    userId: '00000000-0000-0000-0000-000000000002',
    name: { firstName: 'Michael', middleName: null, lastName: 'Scott' },
    login: 'm.scott',
    email: 'm.scott@rpm.local',
    status: 'PENDING',
    audit: {
      createdAt: '2026-06-20T11:02:09.310Z',
      createdBy: SYSTEM_ACTOR,
      updatedAt: '2026-06-21T13:33:27.064Z',
      updatedBy: JOHN_DOE,
    },
  },
  {
    userId: '00000000-0000-0000-0000-000000000003',
    name: { firstName: 'Emily', middleName: null, lastName: 'Carter' },
    login: 'e.carter',
    email: 'e.carter@rpm.local',
    status: 'LOCKED',
    audit: {
      createdAt: '2026-06-16T16:45:02.733Z',
      createdBy: JOHN_DOE,
      updatedAt: '2026-06-18T10:21:44.150Z',
      updatedBy: SYSTEM_ACTOR,
    },
  },
  {
    userId: '00000000-0000-0000-0000-000000000004',
    name: { firstName: 'David', middleName: null, lastName: 'Lee' },
    login: 'd.lee',
    email: 'd.lee@rpm.local',
    status: 'INACTIVE',
    audit: {
      createdAt: '2026-06-12T09:14:37.482Z',
      createdBy: SYSTEM_ACTOR,
      updatedAt: '2026-06-15T16:50:13.628Z',
      updatedBy: SARAH_CONNOR,
    },
  },
];

// Expected rendered cell values, in render order — the single source of truth the grid
// assertions compare against. Full name includes the middle name when present; audit
// actors render as "{firstInitial}. {lastName}", and the seed actor as "System".
export const EXPECTED_USER_ROWS: readonly ExpectedUserRow[] = [
  {
    name: 'Sarah Jane Connor',
    login: 's.connor',
    email: 's.connor@rpm.local',
    status: 'Active',
    createdBy: 'J. Doe',
    updatedBy: 'S. Connor',
  },
  {
    name: 'Michael Scott',
    login: 'm.scott',
    email: 'm.scott@rpm.local',
    status: 'Pending',
    createdBy: SEED_ACTOR_DISPLAY,
    updatedBy: 'J. Doe',
  },
  {
    name: 'Emily Carter',
    login: 'e.carter',
    email: 'e.carter@rpm.local',
    status: 'Locked',
    createdBy: 'J. Doe',
    updatedBy: SEED_ACTOR_DISPLAY,
  },
  {
    name: 'David Lee',
    login: 'd.lee',
    email: 'd.lee@rpm.local',
    status: 'Inactive',
    createdBy: SEED_ACTOR_DISPLAY,
    updatedBy: 'S. Connor',
  },
];

// "ar" is a "contains" probe for the Full name column filter: it appears mid-word in
// "S(ar)ah Jane Connor" and "Emily C(ar)ter", but not in "Michael Scott" or "David Lee",
// so the client-side filter must keep exactly those two rows in their original render order.
export const FULL_NAME_FILTER_TERM = 'ar';

// The full names that survive FULL_NAME_FILTER_TERM, in render order. HAND-LISTED, not computed via
// the production `.includes()` predicate — deriving the expectation by re-running the filter under
// test is the "smart test" anti-pattern (a buggy contains-filter would produce a matching-buggy
// expectation). Same reason as FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY / STATUSES_IN_LIFECYCLE_ORDER.
export const FULL_NAMES_MATCHING_FILTER: readonly string[] = ['Sarah Jane Connor', 'Emily Carter'];

// Scenario 3.4 — multi-column AND filtering. Two "contains" probes on TWO DIFFERENT text columns,
// chosen so neither term alone isolates the target — only their AND does:
//   • 'e' on Login keeps Emily Carter + David Lee;  'connor' on Updated-by keeps Sarah + David.
//   • David Lee is the only row in both sets, so the AND survivor is exactly [David Lee].
// Survivor is HAND-LISTED, not computed via the AND under test — same reason as STATUSES_IN_LIFECYCLE_ORDER.
export const LOGIN_FILTER_TERM = 'e';
export const UPDATED_BY_FILTER_TERM = 'connor';
export const FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY: readonly string[] = ['David Lee'];

// Scenario 3.6 — the Status column filter is a lifecycle-ordered multi-select (not a text "contains"
// input). The selection under test is Pending + Locked; the fixture holds exactly one row per status,
// so those two statuses map to exactly the two rows below.
//   Fixture rows in render order (createdAt DESC):
//     s.connor  ACTIVE   -> 'Active'
//     m.scott   PENDING  -> 'Pending'   ✔ selected
//     e.carter  LOCKED   -> 'Locked'    ✔ selected
//     d.lee     INACTIVE -> 'Inactive'
// Michael Scott (Pending) renders BEFORE Emily Carter (Locked) because createdAt DESC keeps the
// original render order for the surviving rows — the multi-select filters, it does not reorder.
export const STATUS_FILTER_SELECTION: readonly string[] = ['Pending', 'Locked'];

// The full names that survive selecting Pending + Locked, in render order. HAND-LISTED, not computed by
// running the status predicate over the fixture — deriving the expectation by re-running the filter
// under test is the "smart test" anti-pattern (a buggy status-filter would produce a matching-buggy
// expectation). Same reason as STATUSES_IN_LIFECYCLE_ORDER / FULL_NAMES_MATCHING_LOGIN_AND_UPDATED_BY.
export const FULL_NAMES_MATCHING_STATUS_FILTER: readonly string[] = ['Michael Scott', 'Emily Carter'];

// Column-sort expectations (LOGINS_ASCENDING/DESCENDING, LOGINS_BY_CREATED_INSTANT_*,
// STATUSES_IN_LIFECYCLE_ORDER) live in ./admin-users-sort.fixture.ts to keep both files under the cap.

export type AuditActorField = 'createdBy' | 'updatedBy';

export interface SeedActorCell {
  readonly rowIndex: number;
  readonly field: AuditActorField;
}

const AUDIT_ACTOR_FIELDS: readonly AuditActorField[] = ['createdBy', 'updatedBy'];

// The exact audit-actor cells whose value is the seed actor, derived from EXPECTED_USER_ROWS
// so the "seed actor -> System" assertion stays in lock-step with the row data and needs no
// branching in the Statements layer.
export const SEED_ACTOR_CELLS: readonly SeedActorCell[] = EXPECTED_USER_ROWS.flatMap((row, rowIndex) =>
  AUDIT_ACTOR_FIELDS.filter((field) => row[field] === SEED_ACTOR_DISPLAY).map((field) => ({ rowIndex, field })),
);
