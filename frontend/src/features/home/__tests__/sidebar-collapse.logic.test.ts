import { describe, expect, it } from 'vitest';
import { parseSidebarCollapsedState } from '../logic/sidebar-collapse.logic';

describe('Sidebar Collapse State Parsing', () => {
  it.each([
    { name: 'defaults to expanded on first visit when nothing is stored', raw: null, expected: false },
    { name: 'parses a stored "true" as collapsed', raw: 'true', expected: true },
    { name: 'parses a stored "false" as expanded', raw: 'false', expected: false },
    { name: 'treats an unrecognized stored value as expanded', raw: 'maybe', expected: false },
  ])('$name', ({ raw, expected }) => {
    expect(parseSidebarCollapsedState(raw)).toBe(expected);
  });
});
