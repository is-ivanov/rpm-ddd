import type { UserSummaryResponse } from './users-grid.types';

export function fetchAdminUsers(): Promise<UserSummaryResponse[]> {
  return Promise.reject(new Error('Not implemented'));
}
