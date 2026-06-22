import { describe, expect, it } from 'vitest';
import { buildDashboardUser } from '../logic/dashboard-user.logic';
import type { AuthenticatedUser } from '../logic/types';

const IVAN_PETROV: AuthenticatedUser = {
  login: 'ipetrov',
  email: 'i.petrov@rpm.local',
  firstName: 'Иван',
  lastName: 'Петров',
};

const LOWERCASE_NAME: AuthenticatedUser = {
  login: 'asidorova',
  email: 'a.sidorova@rpm.local',
  firstName: 'анна',
  lastName: 'сидорова',
};

describe('Dashboard User View Model', () => {
  // RED — buildDashboardUser stub returns empty strings; displayName/initials derivation not implemented yet (green-frontend).
  it.fails('derives the display name and avatar initials from the authenticated user', () => {
    const dashboardUser = buildDashboardUser(IVAN_PETROV);

    expect(dashboardUser.displayName).toBe('Иван Петров');
    expect(dashboardUser.initials).toBe('ИП');
  });

  // RED — initials must be UPPERCASED first letters; stub returns '' so this fails until green-frontend.
  it.fails('uppercases the initials even when the name is entered in lowercase', () => {
    const dashboardUser = buildDashboardUser(LOWERCASE_NAME);

    expect(dashboardUser.displayName).toBe('анна сидорова');
    expect(dashboardUser.initials).toBe('АС');
  });
});
