import { describe, expect, it } from 'vitest';
import { toAbsoluteTooltipParts, toRelativeTimeLabel } from '../logic/users-grid.logic';

describe('Relative time label', () => {
  // RED — toRelativeTimeLabel returns pass-through ISO string instead of relative label
  it.fails('converts an ISO timestamp to "X days ago" relative to a now reference', () => {
    const result = toRelativeTimeLabel('2026-06-12T09:14:37.482Z', new Date('2026-06-29T12:34:56.789Z'));

    expect(result).toBe('17 days ago');
  });

  // RED — toRelativeTimeLabel returns pass-through ISO string instead of "just now"
  it.fails('returns "just now" for a timestamp less than a minute old', () => {
    const result = toRelativeTimeLabel('2026-06-29T12:34:26.789Z', new Date('2026-06-29T12:34:56.789Z'));

    expect(result).toBe('just now');
  });
});

describe('Absolute time tooltip parts', () => {
  // RED — toAbsoluteTooltipParts returns empty strings for all fields
  it.fails('formats the date as ISO yyyy-MM-dd in the viewer timezone', () => {
    const parts = toAbsoluteTooltipParts('2026-06-12T09:14:37.482Z', 'Europe/Berlin');

    expect(parts.date).toBe('2026-06-12');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for all fields
  it.fails('formats the time as HH:mm 24h in the viewer timezone', () => {
    const parts = toAbsoluteTooltipParts('2026-06-12T09:14:37.482Z', 'Europe/Berlin');

    expect(parts.time).toBe('11:14');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for all fields
  it.fails('emits the short timezone abbreviation for summer (CEST)', () => {
    const parts = toAbsoluteTooltipParts('2026-06-12T09:14:37.482Z', 'Europe/Berlin');

    expect(parts.tzLabel).toBe('CEST');
  });

  // RED — toAbsoluteTooltipParts returns empty strings for all fields
  it.fails('passes through the IANA zone id verbatim', () => {
    const parts = toAbsoluteTooltipParts('2026-06-12T09:14:37.482Z', 'Europe/Berlin');

    expect(parts.ianaZone).toBe('Europe/Berlin');
  });
});
