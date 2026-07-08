// Created date-range filter expectations for the Users grid (Scenario 3.7), split out of
// admin-users-fixture.ts (already at the 200-line cap) so both files stay under it.
//
// The Created column filter is a from–to date range that narrows the grid by the underlying
// createdAt INSTANT — not by the rendered relative-time label ("6 days ago"). The survivor set
// below is a HAND-LISTED literal, never computed by re-running a range predicate over the fixture:
// deriving the expectation by re-running the filter under test is the "smart test" anti-pattern (a
// buggy range-filter would produce a matching-buggy expectation the assertion could never catch).
// Same discipline as STATUSES_IN_LIFECYCLE_ORDER / FULL_NAMES_MATCHING_STATUS_FILTER.
//
// Fixture rows in render order (SEVERAL_ADMIN_USERS, createdAt DESC), for reconfirming the literal:
//   s.connor  createdAt 2026-06-22T14:30:51.217Z  "Sarah Jane Connor"  → EXCLUDED (after `to` 06-21)
//   m.scott   createdAt 2026-06-20T11:02:09.310Z  "Michael Scott"      → included
//   e.carter  createdAt 2026-06-16T16:45:02.733Z  "Emily Carter"       → included
//   d.lee     createdAt 2026-06-12T09:14:37.482Z  "David Lee"          → EXCLUDED (before `from` 06-15)
//
// Range under test: Created from 2026-06-15 to 2026-06-21 (inclusive). Values are HTML date-input
// strings (YYYY-MM-DD).
export const CREATED_RANGE_FROM = '2026-06-15';
export const CREATED_RANGE_TO = '2026-06-21';

// The full names that survive the 2026-06-15 → 2026-06-21 range, in render order. HAND-LISTED.
// Discrimination — verified by hand against the fixture; this survivor set is:
//   (1) REACHABLE only by comparing the underlying createdAt INSTANT to the two date bounds
//       (m.scott 06-20 and e.carter 06-16 fall inside; s.connor 06-22 and d.lee 06-12 fall outside).
//   (2) DISTINCT from the unfiltered set (all 4 rows) — so the RED, where no date-range control
//       exists and all 4 rows stay visible, genuinely fails against this 2-row expectation.
//   (3) NOT derivable from the rendered RELATIVE-TIME label ("6 days ago" / "1 week ago" / …). Those
//       labels are relative to the current clock and do not encode the absolute day, so no operation
//       on the label text could select exactly these two rows by the calendar bounds above — proving
//       the filter "operates on the absolute instant, not the relative label".
export const FULL_NAMES_IN_CREATED_RANGE: readonly string[] = ['Michael Scott', 'Emily Carter'];
