import { describe, expect, it } from 'vitest';
import { buildDashboardUser } from '../logic/dashboard-user.logic';
import { anAuthenticatedUser } from '@/test/builders/authenticated-user';

describe('Dashboard User View Model', () => {
  it('derives the display name and avatar initials from the authenticated user', () => {
    const authenticatedUser = anAuthenticatedUser({ firstName: 'John', lastName: 'Doe' });

    const dashboardUser = buildDashboardUser(authenticatedUser);

    expect(dashboardUser.displayName).toBe('John Doe');
    expect(dashboardUser.initials).toBe('JD');
  });

  it('uppercases the initials even when the name is entered in lowercase', () => {
    const authenticatedUser = anAuthenticatedUser({ firstName: 'jane', lastName: 'smith' });

    const dashboardUser = buildDashboardUser(authenticatedUser);

    expect(dashboardUser.displayName).toBe('jane smith');
    expect(dashboardUser.initials).toBe('JS');
  });
});
