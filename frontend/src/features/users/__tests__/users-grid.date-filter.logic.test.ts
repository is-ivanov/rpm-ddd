import { describe, expect, it } from 'vitest';
import { buildUserRows, filterRowsByDateRange } from '../logic/users-grid.logic';
import type { UserAudit } from '../logic/users-grid.types';
import {
  DAVID_LEE,
  EMILY_CARTER,
  JOHN_DOE,
  MICHAEL_SCOTT,
  SARAH_CONNOR,
  aUserSummary,
} from '@/test/builders/user-summary';

// Scn 3.7 — the Created/Updated date-range filter narrows the grid by the underlying createdAt/updatedAt
// INSTANT, not by the rendered relative-time label. Range under test: 2026-06-15 → 2026-06-21, inclusive.
// Every expected survivor set is a HAND-LISTED literal (same rows as admin-users-date-filter.fixture.ts) —
// never derived by re-running filterRowsByDateRange/sort/map over the fixture (smart-test anti-pattern).
// Survivors are asserted by `login` — a pass-through row field (login: user.login) — so this test stays
// decoupled from name-formatting (toFullName), the way the sibling users-grid.sort.logic.test.ts does.
const CREATED_FROM = '2026-06-15';
const CREATED_TO = '2026-06-21';

describe('Created date-range filter (by the underlying created instant)', () => {
  // Fixture rows (login | createdAt), mirroring SEVERAL_ADMIN_USERS:
  //   s.connor  2026-06-22T14:30:51.217Z  "Sarah Jane Connor"  → EXCLUDED (after `to` 06-21)
  //   m.scott   2026-06-20T11:02:09.310Z  "Michael Scott"      → included
  //   e.carter  2026-06-16T16:45:02.733Z  "Emily Carter"       → included
  //   d.lee     2026-06-12T09:14:37.482Z  "David Lee"          → EXCLUDED (before `from` 06-15)
  const rows = buildUserRows([
    aUserSummary({ name: SARAH_CONNOR, login: 's.connor', audit: createdAt('2026-06-22T14:30:51.217Z') }),
    aUserSummary({ name: MICHAEL_SCOTT, login: 'm.scott', audit: createdAt('2026-06-20T11:02:09.310Z') }),
    aUserSummary({ name: EMILY_CARTER, login: 'e.carter', audit: createdAt('2026-06-16T16:45:02.733Z') }),
    aUserSummary({ name: DAVID_LEE, login: 'd.lee', audit: createdAt('2026-06-12T09:14:37.482Z') }),
  ]);

  it('keeps only rows whose created instant falls inside the from–to range', () => {
    const filtered = filterRowsByDateRange(rows, 'created', CREATED_FROM, CREATED_TO);

    expect(
      filtered.map((row) => row.login),
      'only m.scott (06-20) and e.carter (06-16) are inside 06-15→06-21; s.connor (06-22) and d.lee (06-12) are outside',
    ).toEqual(['m.scott', 'e.carter']);
  });

  // Boundary rows: a user created late on the `from` day (00:12) and late on the `to` day (23:10) must
  // BOTH survive — a naive `new Date(to)` (midnight) impl drops the 23:10 to-day user, and a naive lower
  // bound could drop the 00:12 from-day user. This is the inclusive-bound pin.
  it('treats both bounds as inclusive across the whole from and to days', () => {
    const boundaryRows = buildUserRows([
      aUserSummary({ name: MICHAEL_SCOTT, login: 'm.scott', audit: createdAt('2026-06-15T00:12:07.334Z') }),
      aUserSummary({ name: JOHN_DOE, login: 'j.doe', audit: createdAt('2026-06-21T23:10:44.612Z') }),
      aUserSummary({ name: SARAH_CONNOR, login: 's.connor', audit: createdAt('2026-06-22T14:30:51.217Z') }),
      aUserSummary({ name: DAVID_LEE, login: 'd.lee', audit: createdAt('2026-06-12T09:14:37.482Z') }),
    ]);

    const filtered = filterRowsByDateRange(boundaryRows, 'created', CREATED_FROM, CREATED_TO);

    expect(
      filtered.map((row) => row.login),
      'inclusive bounds — a naive midnight new Date(to) impl drops the 06-21T23:10 to-day user and fails this',
    ).toEqual(['m.scott', 'j.doe']);
  });
});

describe('Updated date-range filter (column selects which audit instant to compare)', () => {
  // Each row is constructed so createdAt and updatedAt land on opposite sides of the range — filtering by
  // 'updated' must therefore yield a DIFFERENT survivor set than filtering by 'created' would.
  //   e.carter  created 2026-06-16 (in-range)  updated 2026-06-22 (after `to`)  → EXCLUDED by updated
  //   m.scott   created 2026-06-12 (before)    updated 2026-06-20 (in-range)    → included by updated
  const rows = buildUserRows([
    aUserSummary({
      name: EMILY_CARTER,
      login: 'e.carter',
      audit: audit('2026-06-16T16:45:02.733Z', '2026-06-22T14:30:51.217Z'),
    }),
    aUserSummary({
      name: MICHAEL_SCOTT,
      login: 'm.scott',
      audit: audit('2026-06-12T09:14:37.482Z', '2026-06-20T11:02:09.310Z'),
    }),
  ]);

  it('compares the updated instant, not the created instant, when column is "updated"', () => {
    const filtered = filterRowsByDateRange(rows, 'updated', CREATED_FROM, CREATED_TO);

    expect(
      filtered.map((row) => row.login),
      "column 'updated' must select updatedAt — m.scott (updated 06-20) survives, e.carter (updated 06-22) does not",
    ).toEqual(['m.scott']);
  });
});

function createdAt(instant: string): UserAudit {
  return audit(instant, '2026-06-24T08:11:42.905Z');
}

function audit(createdAtInstant: string, updatedAtInstant: string): UserAudit {
  return {
    createdAt: createdAtInstant,
    createdBy: JOHN_DOE,
    updatedAt: updatedAtInstant,
    updatedBy: SARAH_CONNOR,
  };
}
