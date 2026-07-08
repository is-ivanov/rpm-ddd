import { describe, expect, it } from 'vitest';
import {
  buildUserRows,
  filterRowsByColumns,
  filterRowsByFullName,
  filterRowsByStatuses,
} from '../logic/users-grid.logic';
import {
  DAVID_LEE,
  EMILY_CARTER,
  JOHN_DOE,
  MICHAEL_SCOTT,
  SARAH_CONNOR,
  SYSTEM_ACTOR,
  aUserSummary,
} from '@/test/builders/user-summary';

describe('Users grid view model', () => {
  it.each([
    { status: 'ACTIVE', label: 'Active' },
    { status: 'PENDING', label: 'Pending' },
    { status: 'LOCKED', label: 'Locked' },
    { status: 'INACTIVE', label: 'Inactive' },
  ])('maps status code $status to label $label', ({ status, label }) => {
    const [row] = buildUserRows([aUserSummary({ status })]);

    expect(row.status).toBe(label);
  });

  it('abbreviates a normal audit actor as "{firstInitial}. {lastName}"', () => {
    const [row] = buildUserRows([aUserSummary({})]);

    expect(row.createdBy).toBe('J. Doe');
    expect(row.updatedBy).toBe('S. Connor');
  });

  // The System actor (empty last name) renders verbatim; a normal updatedBy still abbreviates.
  it('renders the seed/System actor verbatim as "System" (empty last name)', () => {
    const seeded = aUserSummary({
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
    const [row] = buildUserRows([aUserSummary({ name: SARAH_CONNOR })]);

    expect(row.name).toBe('Sarah Jane Connor');
  });

  it('composes the full name without a middle name when absent', () => {
    const [row] = buildUserRows([aUserSummary({ name: MICHAEL_SCOTT })]);

    expect(row.name).toBe('Michael Scott');
  });
});

describe('Full name column filter', () => {
  const fourRows = buildUserRows([
    aUserSummary({ name: SARAH_CONNOR }),
    aUserSummary({ name: MICHAEL_SCOTT }),
    aUserSummary({ name: EMILY_CARTER }),
    aUserSummary({ name: DAVID_LEE }),
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
    aUserSummary({ name: SARAH_CONNOR, login: 's.connor' }),
    aUserSummary({ name: MICHAEL_SCOTT, login: 'm.scott' }),
    aUserSummary({ name: EMILY_CARTER, login: 'e.carter' }),
    aUserSummary({ name: DAVID_LEE, login: 'd.lee' }),
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

describe('Status column filter (multi-select, set membership)', () => {
  // Four rows in a deliberately MIXED status order so a subset filter is observable (not equal to a
  // pass-through): buildUserRows maps ACTIVE→'Active', PENDING→'Pending', LOCKED→'Locked',
  // INACTIVE→'Inactive'. Selecting {Pending, Locked} must keep exactly {m.scott, e.carter} in render
  // order — a pass-through keeps all 4, an OR-with-everything keeps all 4, a single-status impl keeps 1.
  const rows = buildUserRows([
    aUserSummary({ login: 's.connor', status: 'ACTIVE' }),
    aUserSummary({ login: 'm.scott', status: 'PENDING' }),
    aUserSummary({ login: 'e.carter', status: 'LOCKED' }),
    aUserSummary({ login: 'd.lee', status: 'INACTIVE' }),
  ]);

  it('keeps only rows whose status is in the selected set, preserving render order', () => {
    const filtered = filterRowsByStatuses(rows, ['Pending', 'Locked']);

    expect(
      filtered.map((row) => row.login),
      'set membership over {Pending, Locked} — a pass-through / single-status / OR-with-all impl would differ',
    ).toEqual(['m.scott', 'e.carter']);
    expect(filtered.map((row) => row.status)).toEqual(['Pending', 'Locked']);
  });

  it('returns all rows unchanged for an empty selection (no active status filter)', () => {
    const filtered = filterRowsByStatuses(rows, []);

    expect(
      filtered.map((row) => row.login),
      'empty selection is the no-filter pass-through guard, mirroring a blank text term',
    ).toEqual(['s.connor', 'm.scott', 'e.carter', 'd.lee']);
  });
});
