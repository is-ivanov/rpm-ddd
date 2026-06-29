import { EXPECTED_USER_ROWS } from './admin-users-fixture';

// --- Scenario 3.3: Created-cell relative time + hover tooltip -----------------
// Viewer profile timezone from the mocked /api/auth/me (Scn 4.1 default "Central Europe" -> IANA).
export const VIEWER_TIME_ZONE_ID = 'Europe/Berlin';
// Frozen E2E clock instant (non-round per test-data realism); the Created cell computes its relative
// label against THIS instant, so the value is deterministic regardless of CI time.
export const FIXED_NOW_INSTANT = '2026-06-29T12:34:56.789Z';
// Row index of David Lee (login 'd.lee'), whose createdAt is 17.14 days before FIXED_NOW_INSTANT
// -> floor and round both yield "17 days ago". DERIVED from EXPECTED_USER_ROWS by login so it stays
// lock-step if the fixture is reordered -- same pattern as FULL_NAMES_MATCHING_FILTER et al.
export const RELATIVE_TIME_ROW_INDEX = EXPECTED_USER_ROWS.findIndex((row) => row.login === 'd.lee');
// For David Lee's createdAt (2026-06-12T09:14:37.482Z) in Europe/Berlin summer (CEST, UTC+2 =>
// 11:14 local). Label + tz-label + IANA id are pinned exact (the contract); date/time are loose
// contains probes (absolute format is a green/align-design decision) that still discriminate --
// the time distinguishes CEST (11:14) from raw UTC (09:14).
export const EXPECTED_RELATIVE_LABEL = '17 days ago';
export const EXPECTED_TOOLTIP_DATE_FRAGMENT = '2026-06-12';
export const EXPECTED_TOOLTIP_TIME_FRAGMENT = '11:14';
export const EXPECTED_TOOLTIP_TZ_LABEL = 'CEST';
