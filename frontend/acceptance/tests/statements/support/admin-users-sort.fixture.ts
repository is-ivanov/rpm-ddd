import { EXPECTED_USER_ROWS, SEVERAL_ADMIN_USERS } from './admin-users-fixture';

// Column-sort expectations for the Users grid, split out of admin-users-fixture.ts so both files stay
// under the 200-line cap. Each expected order is DERIVED from the fixture data (never hand-maintained,
// except the lifecycle literal below whose ordering IS the business rule under test).

// Expected Login-column ordering after clicking the Login header, derived from EXPECTED_USER_ROWS
// (not hand-maintained) so the sort assertions stay in lock-step with the row data: ascending is
// the logins sorted, descending is that same ordering reversed. The default render order is
// createdAt DESC, so ascending differs from it — that difference is what the RED assertion catches.
export const LOGINS_ASCENDING: readonly string[] = EXPECTED_USER_ROWS.map((row) => row.login).toSorted((left, right) =>
  left.localeCompare(right),
);

export const LOGINS_DESCENDING: readonly string[] = LOGINS_ASCENDING.toReversed();

// Scenario 3.5 — the genuinely-NEW sort category: the Created column sorts by the underlying
// createdAt INSTANT, not by the rendered relative-time label (lexical/lifecycle sorts are already
// proven by Scn 3.2's Login/Status). Expected Login-cell ordering after clicking the Created header,
// DERIVED from SEVERAL_ADMIN_USERS by createdAt (never hand-listed) so it stays lock-step with the
// fixture: ascending = oldest createdAt first; descending = that ordering reversed. The Login cell is
// the assertion vehicle (unique, unambiguous) — the relative-time text is tied/ambiguous.
//
// Discrimination — verified by hand against the fixture; the expected ASCENDING order is:
//   (1) REACHABLE by a correct instant sort — it IS the createdAt-ascending order.
//   (2) DISTINCT from the default render order (createdAt DESC = [s.connor, m.scott, e.carter, d.lee]);
//       ascending is its exact reverse [d.lee, e.carter, m.scott, s.connor], so the RED — where Created
//       has no comparator and rows stay in default DESC — genuinely fails.
//   (3) DISTINCT from a lexical sort of the RELATIVE-LABEL text ("by instant, not the relative label").
//       Labels against FIXED_NOW_INSTANT (2026-06-29T12:34:56.789Z): s.connor "6 days ago",
//       m.scott "1 week ago", e.carter "1 week ago", d.lee "2 weeks ago". Sorting those label STRINGS
//       ascending yields [m.scott, e.carter, d.lee, s.connor] (because '1' < '2' < '6'), which diverges
//       from the instant order at position 0 (m.scott vs d.lee) — a label-text sort cannot pass this
//       assertion. (The assertion is on Login cells, so the running test needs no frozen clock; the
//       labels above document the discrimination only.)
export const LOGINS_BY_CREATED_INSTANT_ASCENDING: readonly string[] = [...SEVERAL_ADMIN_USERS]
  .toSorted((left, right) => Date.parse(left.audit.createdAt) - Date.parse(right.audit.createdAt))
  .map((user) => user.login);

export const LOGINS_BY_CREATED_INSTANT_DESCENDING: readonly string[] = LOGINS_BY_CREATED_INSTANT_ASCENDING.toReversed();

// The Status column sorts by lifecycle order — Pending, Active, Locked, Inactive — NOT
// alphabetically. This ordering IS the business rule under test, so the expected Status-column
// sequence is HAND-LISTED as an explicit literal — never derived by sorting the row statuses with
// the production lifecycle comparator. Replicating the rule to compute the expectation would make
// the test "smart": a buggy lifecycle sort could still match an expectation produced the same buggy
// way. The fixture holds exactly one row per status, so after the lifecycle sort the rendered Status
// column is precisely this sequence.
export const STATUSES_IN_LIFECYCLE_ORDER: readonly string[] = ['Pending', 'Active', 'Locked', 'Inactive'];
