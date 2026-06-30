import { describe, expect, it } from 'vitest';
import { buildDashboardUser } from '../logic/dashboard-user.logic';
import type { AuthenticatedUser } from '../logic/current-user.types';

const JOHN_DOE: AuthenticatedUser = {
  login: 'jdoe',
  email: 'j.doe@rpm.local',
  firstName: 'John',
  lastName: 'Doe',
  timeZone: 'Europe/Berlin',
};

const LOWERCASE_NAME: AuthenticatedUser = {
  login: 'jsmith',
  email: 'j.smith@rpm.local',
  firstName: 'jane',
  lastName: 'smith',
  timeZone: 'Europe/Berlin',
};

describe('Dashboard User View Model', () => {
  it('derives the display name and avatar initials from the authenticated user', () => {
    const dashboardUser = buildDashboardUser(JOHN_DOE);

    expect(dashboardUser.displayName).toBe('John Doe');
    expect(dashboardUser.initials).toBe('JD');
  });

  it('uppercases the initials even when the name is entered in lowercase', () => {
    const dashboardUser = buildDashboardUser(LOWERCASE_NAME);

    expect(dashboardUser.displayName).toBe('jane smith');
    expect(dashboardUser.initials).toBe('JS');
  });
});
