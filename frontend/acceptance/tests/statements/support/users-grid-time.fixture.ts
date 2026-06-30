import { EXPECTED_USER_ROWS } from './admin-users-fixture';

// --- Scenario 3.3: Created-cell relative time + hover tooltip -----------------
// Viewer profile timezone from the mocked /api/auth/me (Scn 4.1 default "Central Europe" -> IANA).
export const VIEWER_TIME_ZONE_ID = 'Europe/Berlin';
// Frozen E2E clock instant (non-round per test-data realism); the Created cell computes its relative
// label against THIS instant, so the value is deterministic regardless of CI time.
export const FIXED_NOW_INSTANT = '2026-06-29T12:34:56.789Z';
// Row index of David Lee (login 'd.lee'), whose createdAt is 17.139 days before FIXED_NOW_INSTANT.
// Under the B1 relative-time contract (days < 7d, then weeks/months/years with floor rounding),
// 17.139 days -> floor(17.139 / 7) = 2 -> "2 weeks ago" (round also yields 2, so the value is
// robust mid-bucket: 2.448 weeks sits clear of both the 2- and 3-week boundaries). DERIVED from
// EXPECTED_USER_ROWS by login so it stays lock-step if the fixture is reordered -- same pattern as
// FULL_NAMES_MATCHING_FILTER et al.
export const RELATIVE_TIME_ROW_INDEX = EXPECTED_USER_ROWS.findIndex((row) => row.login === 'd.lee');
// For David Lee's createdAt (2026-06-12T09:14:37.482Z) in Europe/Berlin summer (CEST, UTC+2 =>
// 11:14 local). Label + tz-label + IANA id are pinned exact (the contract); date/time are loose
// contains probes (absolute format is a green/align-design decision) that still discriminate --
// the time distinguishes CEST (11:14) from raw UTC (09:14). The CEST/CET abbreviation is only
// emitted by Intl under an en-GB locale (en-US yields "GMT+2"), so the green formatter pins en-GB.
export const EXPECTED_RELATIVE_LABEL = '2 weeks ago';
export const EXPECTED_TOOLTIP_DATE_FRAGMENT = '2026-06-12';
export const EXPECTED_TOOLTIP_TIME_FRAGMENT = '11:14';
export const EXPECTED_TOOLTIP_TZ_LABEL = 'CEST';
