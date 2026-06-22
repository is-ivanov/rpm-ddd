import type { AuthenticatedUser } from './types';

export interface DashboardUser {
  readonly displayName: string;
  readonly initials: string;
}

export function buildDashboardUser(_user: AuthenticatedUser): DashboardUser {
  return { displayName: '', initials: '' };
}
