import { describe, expect, it } from 'vitest';
import { toAbsoluteTooltipParts, toRelativeTimeLabel } from '../logic/users-grid.logic';

// Fixed reference instant for every relative-label case (matches the E2E fixture FIXED_NOW_INSTANT).
const NOW = new Date('2026-06-29T12:34:56.789Z');

describe('Relative time label', () => {
  // RED — toRelativeTimeLabel returns the raw ISO string instead of a bucketed relative label.
  // Each row pins one point of the fixed B1 scale (floor rounding, singular at a count of 1):
  // just now < 60s, minutes < 60m, hours < 24h, days < 7d, weeks < 30d, months < 365d, years.
  // The anchor ISO strings are NOW minus an exact offset (pre-computed, no arithmetic in the test).
  it.fails.each([
    { iso: '2026-06-29T12:34:26.789Z', expected: 'just now', bucket: '30s -> just now' },
    { iso: '2026-06-29T12:33:56.789Z', expected: '1 minute ago', bucket: '60s -> singular minute' },
    { iso: '2026-06-29T12:29:56.789Z', expected: '5 minutes ago', bucket: '5m -> plural minutes' },
    { iso: '2026-06-29T11:34:56.789Z', expected: '1 hour ago', bucket: '60m -> singular hour' },
    { iso: '2026-06-29T09:34:56.789Z', expected: '3 hours ago', bucket: '3h -> plural hours' },
    { iso: '2026-06-28T12:34:56.789Z', expected: '1 day ago', bucket: '24h -> singular day' },
    { iso: '2026-06-26T12:34:56.789Z', expected: '3 days ago', bucket: '3d -> plural days' },
    { iso: '2026-06-22T12:34:56.789Z', expected: '1 week ago', bucket: '7d -> singular week' },
    { iso: '2026-06-12T09:14:37.482Z', expected: '2 weeks ago', bucket: '17.14d -> plural weeks (David Lee)' },
    { iso: '2026-05-15T12:34:56.789Z', expected: '1 month ago', bucket: '45d -> singular month' },
    { iso: '2026-04-15T12:34:56.789Z', expected: '2 months ago', bucket: '75d -> plural months' },
    { iso: '2025-05-25T12:34:56.789Z', expected: '1 year ago', bucket: '400d -> singular year' },
    { iso: '2024-04-20T12:34:56.789Z', expected: '2 years ago', bucket: '800d -> plural years' },
  ])('labels $bucket', ({ iso, expected }) => {
    expect(toRelativeTimeLabel(iso, NOW)).toBe(expected);
  });
});

describe('Absolute time tooltip parts', () => {
  const SUMMER_BERLIN = toAbsoluteTooltipParts('2026-06-12T09:14:37.482Z', 'Europe/Berlin');

  // RED — toAbsoluteTooltipParts returns empty strings for every field.
  it.fails('formats the date as ISO yyyy-MM-dd in the viewer timezone', () => {
    expect(SUMMER_BERLIN.date).toBe('2026-06-12');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for every field.
  it.fails('formats the time as HH:mm 24h in the viewer timezone (UTC 09:14 -> CEST 11:14)', () => {
    expect(SUMMER_BERLIN.time).toBe('11:14');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for every field.
  it.fails('emits the short timezone abbreviation for summer (CEST)', () => {
    expect(SUMMER_BERLIN.tzLabel).toBe('CEST');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for every field.
  it.fails('passes through the IANA zone id verbatim', () => {
    expect(SUMMER_BERLIN.ianaZone).toBe('Europe/Berlin');
  });

  // RED — winter instant must yield CET, not CEST. The stub returns '', so this also fails RED,
  // but it pins the DST-aware behavior the summer case alone cannot (a hardcoded 'CEST' would pass
  // the summer test yet break here).
  it.fails('emits CET for a winter instant in the same zone (DST aware)', () => {
    const winter = toAbsoluteTooltipParts('2026-01-15T09:14:37.482Z', 'Europe/Berlin');

    expect(winter.tzLabel).toBe('CET');
    expect(winter.time).toBe('10:14');
  });

  // RED — converting a late-evening UTC instant into Asia/Tokyo (UTC+9) rolls the calendar date
  // forward. The stub returns '', so this fails RED, but it pins the date-rollover behavior a
  // same-day case cannot (an implementation that formatted the raw UTC date would show 06-29).
  it.fails('rolls the date forward when the zone offset crosses midnight (Asia/Tokyo)', () => {
    const tokyo = toAbsoluteTooltipParts('2026-06-29T22:47:13.456Z', 'Asia/Tokyo');

    expect(tokyo.date).toBe('2026-06-30');
    expect(tokyo.time).toBe('07:47');
    expect(tokyo.ianaZone).toBe('Asia/Tokyo');
  });
});
