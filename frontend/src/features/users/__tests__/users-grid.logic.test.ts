import { describe, expect, it } from 'vitest';
import { buildUserRows, filterRowsByFullName } from '../logic/users-grid.logic';
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
  // RED — buildUserRows stub passes the raw status code through; expects the human-readable label
  it.each([
    { status: 'ACTIVE', label: 'Active' },
    { status: 'PENDING', label: 'Pending' },
    { status: 'LOCKED', label: 'Locked' },
    { status: 'INACTIVE', label: 'Inactive' },
  ])('maps status code $status to label $label', ({ status, label }) => {
    const [row] = buildUserRows([userWith({ status })]);

    expect(row.status).toBe(label);
  });

  // RED — buildUserRows stub emits the raw firstName; expects the "J. Doe" abbreviation
  it('abbreviates a normal audit actor as "{firstInitial}. {lastName}"', () => {
    const [row] = buildUserRows([userWith({})]);

    expect(row.createdBy).toBe('J. Doe');
    expect(row.updatedBy).toBe('S. Connor');
  });

  // RED — stub has no seed special-case; updatedBy abbreviation ("J. Doe") is unimplemented
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

  // RED — stub emits only firstName; expects "First Middle Last" when a middle name is present
  it('composes the full name including the middle name when present', () => {
    const [row] = buildUserRows([userWith({ name: SARAH_CONNOR })]);

    expect(row.name).toBe('Sarah Jane Connor');
  });

  // RED — stub emits only firstName; expects "First Last" when no middle name
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
