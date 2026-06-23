import { describe, expect, it } from 'vitest';
import { buildDashboardUser } from '../logic/dashboard-user.logic';
import type { AuthenticatedUser } from '../logic/current-user.types';

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
  it('derives the display name and avatar initials from the authenticated user', () => {
    const dashboardUser = buildDashboardUser(IVAN_PETROV);

    expect(dashboardUser.displayName).toBe('Иван Петров');
    expect(dashboardUser.initials).toBe('ИП');
  });

  it('uppercases the initials even when the name is entered in lowercase', () => {
    const dashboardUser = buildDashboardUser(LOWERCASE_NAME);

    expect(dashboardUser.displayName).toBe('анна сидорова');
    expect(dashboardUser.initials).toBe('АС');
  });
});
