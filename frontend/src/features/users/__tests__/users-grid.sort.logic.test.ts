import { describe, expect, it } from 'vitest';
import { buildUserRows, sortUserRows } from '../logic/users-grid.logic';
import type { UserSummaryResponse } from '../logic/users-grid.types';
import {
  DAVID_LEE,
  EMILY_CARTER,
  JOHN_DOE,
  MICHAEL_SCOTT,
  SARAH_CONNOR,
  aUserSummary,
} from '@/test/builders/user-summary';

describe('Column header sort', () => {
  // Starting order is deliberately unsorted by both Login and Status (not ascending,
  // not descending, not lifecycle) so each sort below is genuinely observable — a
  // pass-through that returned the input would fail all three.
  const unsortedRows = buildUserRows([
    aUserSummary({ name: MICHAEL_SCOTT, login: 'm.scott', status: 'PENDING' }),
    aUserSummary({ name: SARAH_CONNOR, login: 's.connor', status: 'ACTIVE' }),
    aUserSummary({ name: DAVID_LEE, login: 'd.lee', status: 'INACTIVE' }),
    aUserSummary({ name: EMILY_CARTER, login: 'e.carter', status: 'LOCKED' }),
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
      aUserSummary({ name: SARAH_CONNOR, login: 's.connor', status: 'ACTIVE' }),
      aUserSummary({ name: MICHAEL_SCOTT, login: 'm.scott', status: 'SUSPENDED' }),
      aUserSummary({ name: EMILY_CARTER, login: 'e.carter', status: 'PENDING' }),
      aUserSummary({ name: DAVID_LEE, login: 'd.lee', status: 'LOCKED' }),
    ]);

    const sorted = sortUserRows(rows, 'status', 'asc');

    expect(sorted.map((row) => row.status)).toEqual(['Pending', 'Active', 'Locked', 'SUSPENDED']);
  });
});

describe('Timestamp column sort (by the underlying Created instant)', () => {
  // Scn 3.5 — the genuinely-new sort category: the Created column sorts by the underlying createdAt
  // INSTANT, not the rendered relative-time label (lexical/lifecycle sorts are already proven above).
  // Input order is neither createdAt-ascending NOR Status-lifecycle order, so the Created sort is
  // genuinely observable and the RED (fall-through to statusRank, no timestamp branch yet) is visibly
  // wrong. Both expected orders are HAND-LISTED literals — never derived by re-sorting with Date.parse
  // / localeCompare / .toSorted (the smart-test mistake recorded in summaries/3.5-every-column-sort.md).
  //
  // Rows (login | status | createdAt):
  //   m.scott  | PENDING  | 2026-03-14T09:12:33.481Z
  //   s.connor | ACTIVE   | 2026-01-05T17:48:02.114Z
  //   d.lee    | INACTIVE | 2026-05-27T22:03:41.859Z
  //   e.carter | LOCKED   | 2026-02-19T06:35:19.742Z
  const rows = buildUserRows([
    userCreatedAt('m.scott', 'PENDING', '2026-03-14T09:12:33.481Z'),
    userCreatedAt('s.connor', 'ACTIVE', '2026-01-05T17:48:02.114Z'),
    userCreatedAt('d.lee', 'INACTIVE', '2026-05-27T22:03:41.859Z'),
    userCreatedAt('e.carter', 'LOCKED', '2026-02-19T06:35:19.742Z'),
  ]);

  // RED — created/updated columns have no comparator yet; sortUserRows falls through to statusRank
  // and yields the lifecycle order instead of the createdAt-instant order.
  it.fails('sorts rows ascending by the Created instant, oldest first', () => {
    const sorted = sortUserRows(rows, 'created', 'asc');

    expect(
      sorted.map((row) => row.login),
      'oldest→newest by createdAt instant; a fall-through-to-Status impl yields the lifecycle order instead',
    ).toEqual(['s.connor', 'e.carter', 'm.scott', 'd.lee']);
  });

  // RED — same reason; descending must reverse the instant order, not the lifecycle order.
  it.fails('sorts rows descending by the Created instant, newest first', () => {
    const sorted = sortUserRows(rows, 'created', 'desc');

    expect(sorted.map((row) => row.login)).toEqual(['d.lee', 'm.scott', 'e.carter', 's.connor']);
  });
});

function userCreatedAt(login: string, status: string, createdAt: string): UserSummaryResponse {
  return aUserSummary({
    login,
    status,
    audit: { createdAt, createdBy: JOHN_DOE, updatedAt: '2026-06-24T08:11:42.905Z', updatedBy: SARAH_CONNOR },
  });
}
