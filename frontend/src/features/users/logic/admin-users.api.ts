import type { UserSummaryResponse } from './users-grid.types';
import { adminUsersSchema } from '@/features/users/schemas/admin-users.schema';
import { apiFetch } from '@/app/logic/fetch.api';

const ADMIN_USERS_PATH = '/api/admin/users';

export async function fetchAdminUsers(): Promise<UserSummaryResponse[]> {
  const response = await apiFetch(ADMIN_USERS_PATH, {
    method: 'GET',
    credentials: 'include',
  });

  return adminUsersSchema.parse(await response.json());
}
