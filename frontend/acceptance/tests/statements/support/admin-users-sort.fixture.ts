// Column-sort expectations for the Users grid, split out of admin-users-fixture.ts so both files stay
// under the 200-line cap. Every expected order is a HAND-LISTED literal — never computed from the row
// data with the production comparator. A test that derives its expectation by re-sorting the fixture
// with the same algorithm it verifies is "smart": a buggy sort would produce a matching-buggy
// expectation and the test would pass. Dumb literals catch that; they also force a human to
// reconfirm the order by hand whenever SEVERAL_ADMIN_USERS is reordered.
//
// Fixture rows in render order (createdAt DESC), for reconfirming the literals below:
//   s.connor  createdAt 2026-06-22T14:30:51.217Z   login s.connor
//   m.scott   createdAt 2026-06-20T11:02:09.310Z   login m.scott
//   e.carter  createdAt 2026-06-16T16:45:02.733Z   login e.carter
//   d.lee     createdAt 2026-06-12T09:14:37.482Z   login d.lee

// Expected Login-column order after clicking the Login header: ascending is alphabetical by login,
// descending is the reverse. The default render order is createdAt DESC, so ascending differs from it —
// that difference is what the Scn 3.2 assertion catches.
export const LOGINS_ASCENDING: readonly string[] = ['d.lee', 'e.carter', 'm.scott', 's.connor'];

export const LOGINS_DESCENDING: readonly string[] = ['s.connor', 'm.scott', 'e.carter', 'd.lee'];

// Scenario 3.5 — the genuinely-NEW sort category: the Created column sorts by the underlying
// createdAt INSTANT, not by the rendered relative-time label (lexical/lifecycle sorts are already
// proven by Scn 3.2's Login/Status). Expected Login-cell order after clicking the Created header:
// ascending = oldest createdAt first (d.lee 06-12 → e.carter 06-16 → m.scott 06-20 → s.connor 06-22),
// descending = the reverse. The Login cell is the assertion vehicle (unique, unambiguous) — the
// relative-time text is tied/ambiguous.
//
// Discrimination — verified by hand against the fixture; the expected ASCENDING order is:
//   (1) REACHABLE by a correct instant sort — it IS the createdAt-ascending order.
//   (2) DISTINCT from the default render order (createdAt DESC = [s.connor, m.scott, e.carter, d.lee]);
//       ascending is its exact reverse, so the RED — where Created has no comparator and rows stay in
//       default DESC — genuinely fails.
//   (3) DISTINCT from a lexical sort of the RELATIVE-LABEL text ("by instant, not the relative label").
//       Labels against FIXED_NOW_INSTANT (2026-06-29T12:34:56.789Z): s.connor "6 days ago",
//       m.scott "1 week ago", e.carter "1 week ago", d.lee "2 weeks ago". Sorting those label STRINGS
//       ascending yields [m.scott, e.carter, d.lee, s.connor] (because '1' < '2' < '6'), which diverges
//       from the instant order at position 0 (m.scott vs d.lee) — a label-text sort cannot pass this
//       assertion. (The assertion is on Login cells, so the running test needs no frozen clock; the
//       labels above document the discrimination only.)
export const LOGINS_BY_CREATED_INSTANT_ASCENDING: readonly string[] = ['d.lee', 'e.carter', 'm.scott', 's.connor'];

export const LOGINS_BY_CREATED_INSTANT_DESCENDING: readonly string[] = ['s.connor', 'm.scott', 'e.carter', 'd.lee'];

// The Status column sorts by lifecycle order — Pending, Active, Locked, Inactive — NOT
// alphabetically. This ordering IS the business rule under test, so the expected Status-column
// sequence is a HAND-LISTED literal — never derived by sorting the row statuses with the production
// lifecycle comparator. The fixture holds exactly one row per status, so after the lifecycle sort the
// rendered Status column is precisely this sequence.
export const STATUSES_IN_LIFECYCLE_ORDER: readonly string[] = ['Pending', 'Active', 'Locked', 'Inactive'];
