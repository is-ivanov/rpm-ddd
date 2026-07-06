import { describe, expect, it } from 'vitest';
import { buildUserRows, filterRowsByColumns, filterRowsByFullName, sortUserRows } from '../logic/users-grid.logic';
import type { PersonName, UserSummaryResponse } from '../logic/users-grid.types';

const JOHN_DOE: PersonName = { firstName: 'John', middleName: 'Robert', lastName: 'Doe' };
const SARAH_CONNOR: PersonName = { firstName: 'Sarah', middleName: 'Jane', lastName: 'Connor' };
const MICHAEL_SCOTT: PersonName = { firstName: 'Michael', middleName: null, lastName: 'Scott' };
const EMILY_CARTER: PersonName = { firstName: 'Emily', middleName: null, lastName: 'Carter' };
const DAVID_LEE: PersonName = { firstName: 'David', middleName: null, lastName: 'Lee' };
const SYSTEM_ACTOR: PersonName = { firstName: 'System', middleName: null, lastName: '' };

function userWith(overrides: Partial<UserSummaryResponse>): UserSummaryResponse {
  return {
    userId: '00000000-0000-0000-0000-000000000001',
    name: SARAH_CONNOR,
    login: 's.connor',
    email: 's.connor@rpm.local',
    status: 'ACTIVE',
    audit: {
      createdAt: '2026-06-22T14:30:51.217Z',
      createdBy: JOHN_DOE,
      updatedAt: '2026-06-24T08:11:42.905Z',
      updatedBy: SARAH_CONNOR,
    },
    ...overrides,
  };
}

describe('Users grid view model', () => {
  it.each([
    { status: 'ACTIVE', label: 'Active' },
    { status: 'PENDING', label: 'Pending' },
    { status: 'LOCKED', label: 'Locked' },
    { status: 'INACTIVE', label: 'Inactive' },
  ])('maps status code $status to label $label', ({ status, label }) => {
    const [row] = buildUserRows([userWith({ status })]);

    expect(row.status).toBe(label);
  });

  it('abbreviates a normal audit actor as "{firstInitial}. {lastName}"', () => {
    const [row] = buildUserRows([userWith({})]);

    expect(row.createdBy).toBe('J. Doe');
    expect(row.updatedBy).toBe('S. Connor');
  });

  // The System actor (empty last name) renders verbatim; a normal updatedBy still abbreviates.
  it('renders the seed/System actor verbatim as "System" (empty last name)', () => {
    const seeded = userWith({
      audit: {
        createdAt: '2026-06-20T11:02:09.310Z',
        createdBy: SYSTEM_ACTOR,
        updatedAt: '2026-06-21T13:33:27.064Z',
        updatedBy: JOHN_DOE,
      },
    });

    const [row] = buildUserRows([seeded]);

    expect(row.createdBy).toBe('System');
    expect(row.updatedBy).toBe('J. Doe');
  });

  it('composes the full name including the middle name when present', () => {
    const [row] = buildUserRows([userWith({ name: SARAH_CONNOR })]);

    expect(row.name).toBe('Sarah Jane Connor');
  });

  it('composes the full name without a middle name when absent', () => {
    const [row] = buildUserRows([userWith({ name: MICHAEL_SCOTT })]);

    expect(row.name).toBe('Michael Scott');
  });
});

describe('Full name column filter', () => {
  const fourRows = buildUserRows([
    userWith({ name: SARAH_CONNOR }),
    userWith({ name: MICHAEL_SCOTT }),
    userWith({ name: EMILY_CARTER }),
    userWith({ name: DAVID_LEE }),
  ]);

  it('keeps only rows whose Full name contains the term, preserving render order', () => {
    const filtered = filterRowsByFullName(fourRows, 'ar');

    expect(filtered.map((row) => row.name)).toEqual(['Sarah Jane Connor', 'Emily Carter']);
  });

  it('matches the term case-insensitively', () => {
    const filtered = filterRowsByFullName(fourRows, 'AR');

    expect(filtered.map((row) => row.name)).toEqual(['Sarah Jane Connor', 'Emily Carter']);
  });

  it('returns all rows unchanged for a blank term', () => {
    const filtered = filterRowsByFullName(fourRows, '   ');

    expect(filtered.map((row) => row.name)).toEqual([
      'Sarah Jane Connor',
      'Michael Scott',
      'Emily Carter',
      'David Lee',
    ]);
  });
});

describe('Multi-column text filter (AND-combined)', () => {
  // Login 'c' matches {s.connor, m.scott, e.carter} (not d.lee); name 'i' matches
  // {Michael Scott, Emily Carter, David Lee} (not Sarah Jane Connor). The two 3-row sets overlap
  // in exactly {m.scott, e.carter}, so each column excludes a row the OTHER includes — proving both
  // participate. AND ⇒ 2 rows; OR ⇒ 4; login-only ⇒ 3; name-only ⇒ 3; pass-through ⇒ 4 — all differ.
  const rows = buildUserRows([
    userWith({ name: SARAH_CONNOR, login: 's.connor' }),
    userWith({ name: MICHAEL_SCOTT, login: 'm.scott' }),
    userWith({ name: EMILY_CARTER, login: 'e.carter' }),
    userWith({ name: DAVID_LEE, login: 'd.lee' }),
  ]);

  it('keeps only rows matching EVERY active column filter (AND, not OR), preserving order', () => {
    const filtered = filterRowsByColumns(rows, { login: 'c', name: 'i' });

    expect(
      filtered.map((row) => row.login),
      'AND of the two 3-row match sets — an OR / single-column / pass-through impl would differ',
    ).toEqual(['m.scott', 'e.carter']);
  });

  it('ignores a blank/whitespace term, applying only the other active column filter', () => {
    const filtered = filterRowsByColumns(rows, { login: '   ', name: 'i' });

    expect(
      filtered.map((row) => row.login),
      "blank Login term ignored ⇒ only name 'i' applied (Michael, Emily, David)",
    ).toEqual(['m.scott', 'e.carter', 'd.lee']);
  });

  it('matches each column term case-insensitively', () => {
    const filtered = filterRowsByColumns(rows, { login: 'C', name: 'I' });

    expect(filtered.map((row) => row.login)).toEqual(['m.scott', 'e.carter']);
  });
});

describe('Column header sort', () => {
  // Starting order is deliberately unsorted by both Login and Status (not ascending,
  // not descending, not lifecycle) so each sort below is genuinely observable — a
  // pass-through that returned the input would fail all three.
  const unsortedRows = buildUserRows([
    userWith({ name: MICHAEL_SCOTT, login: 'm.scott', status: 'PENDING' }),
    userWith({ name: SARAH_CONNOR, login: 's.connor', status: 'ACTIVE' }),
    userWith({ name: DAVID_LEE, login: 'd.lee', status: 'INACTIVE' }),
    userWith({ name: EMILY_CARTER, login: 'e.carter', status: 'LOCKED' }),
  ]);

  it('sorts rows ascending by Login on the first header click', () => {
    const sorted = sortUserRows(unsortedRows, 'login', 'asc');

    expect(sorted.map((row) => row.login)).toEqual(['d.lee', 'e.carter', 'm.scott', 's.connor']);
  });

  it('sorts rows descending by Login on the second header click', () => {
    const sorted = sortUserRows(unsortedRows, 'login', 'desc');

    expect(sorted.map((row) => row.login)).toEqual(['s.connor', 'm.scott', 'e.carter', 'd.lee']);
  });

  it('sorts the Status column by lifecycle order, not alphabetically', () => {
    const sorted = sortUserRows(unsortedRows, 'status', 'asc');

    expect(sorted.map((row) => row.status)).toEqual(['Pending', 'Active', 'Locked', 'Inactive']);
  });

  // An unknown status (a code the FE doesn't map yet, added on the backend) must sort to the
  // end, not corrupt the order via undefined - number = NaN.
  it('places an unknown status last instead of breaking the Status sort', () => {
    const rows = buildUserRows([
      userWith({ name: SARAH_CONNOR, login: 's.connor', status: 'ACTIVE' }),
      userWith({ name: MICHAEL_SCOTT, login: 'm.scott', status: 'SUSPENDED' }),
      userWith({ name: EMILY_CARTER, login: 'e.carter', status: 'PENDING' }),
      userWith({ name: DAVID_LEE, login: 'd.lee', status: 'LOCKED' }),
    ]);

    const sorted = sortUserRows(rows, 'status', 'asc');

    expect(sorted.map((row) => row.status)).toEqual(['Pending', 'Active', 'Locked', 'SUSPENDED']);
  });
});
