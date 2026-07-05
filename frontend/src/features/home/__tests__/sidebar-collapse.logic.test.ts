import { describe, expect, it } from 'vitest';
import { parseSidebarCollapsedState } from '../logic/sidebar-collapse.logic';

describe('Sidebar Collapse State Parsing', () => {
  // RED: stub returns true, so the safe-default case fails; remove it.fails in GREEN once raw === 'true' is implemented.
  it.fails('defaults to expanded (not collapsed) on first visit when nothing is stored', () => {
    expect(parseSidebarCollapsedState(null)).toBe(false);
  });

  it('parses a stored "true" as collapsed', () => {
    expect(parseSidebarCollapsedState('true')).toBe(true);
  });

  it.fails('parses a stored "false" as expanded', () => {
    expect(parseSidebarCollapsedState('false')).toBe(false);
  });

  it.fails('treats an unrecognized stored value as expanded', () => {
    expect(parseSidebarCollapsedState('maybe')).toBe(false);
  });
});
