import type { AuthenticatedUser } from './types';

export interface DashboardUser {
  readonly displayName: string;
  readonly initials: string;
}

export function buildDashboardUser(user: AuthenticatedUser): DashboardUser {
  return {
    displayName: `${user.firstName} ${user.lastName}`,
    initials: `${firstLetter(user.firstName)}${firstLetter(user.lastName)}`.toUpperCase(),
  };
}

function firstLetter(name: string): string {
  return [...name][0] ?? '';
}
