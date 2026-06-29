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

const JOHN_DOE: ActorName = { firstName: 'John', middleName: 'Robert', lastName: 'Doe' };
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

// The full names that survive FULL_NAME_FILTER_TERM, derived from EXPECTED_USER_ROWS (render
// order preserved) so the filter assertion stays in lock-step with the row data and needs no
// hand-maintained literal list.
export const FULL_NAMES_MATCHING_FILTER: readonly string[] = EXPECTED_USER_ROWS.filter((row) =>
  row.name.includes(FULL_NAME_FILTER_TERM),
).map((row) => row.name);

// Expected Login-column ordering after clicking the Login header, derived from EXPECTED_USER_ROWS
// (not hand-maintained) so the sort assertions stay in lock-step with the row data: ascending is
// the logins sorted, descending is that same ordering reversed. The default render order is
// createdAt DESC, so ascending differs from it — that difference is what the RED assertion catches.
export const LOGINS_ASCENDING: readonly string[] = EXPECTED_USER_ROWS.map((row) => row.login).toSorted((left, right) =>
  left.localeCompare(right),
);

export const LOGINS_DESCENDING: readonly string[] = LOGINS_ASCENDING.toReversed();

// The Status column sorts by lifecycle order — Pending, Active, Locked, Inactive — NOT
// alphabetically. This ordering IS the business rule under test, so the expected Status-column
// sequence is HAND-LISTED as an explicit literal — never derived by sorting the row statuses with
// the production lifecycle comparator. Replicating the rule to compute the expectation would make
// the test "smart": a buggy lifecycle sort could still match an expectation produced the same buggy
// way. The fixture holds exactly one row per status, so after the lifecycle sort the rendered Status
// column is precisely this sequence.
export const STATUSES_IN_LIFECYCLE_ORDER: readonly string[] = ['Pending', 'Active', 'Locked', 'Inactive'];

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
