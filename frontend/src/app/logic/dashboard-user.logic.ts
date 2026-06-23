import type { AuthenticatedUser } from './current-user.types';

export interface DashboardUser {
  readonly displayName: string;
  readonly initials: string;
  readonly email: string;
}

export function buildDashboardUser(user: AuthenticatedUser): DashboardUser {
  return {
    displayName: `${user.firstName} ${user.lastName}`,
    initials: `${firstLetter(user.firstName)}${firstLetter(user.lastName)}`.toUpperCase(),
    email: user.email,
  };
}

function firstLetter(name: string): string {
  return [...name][0] ?? '';
}
