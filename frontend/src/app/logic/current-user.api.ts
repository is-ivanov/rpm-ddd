import type { CurrentUserResult } from './current-user.types';
import { currentUserResponseSchema } from '@/app/schemas/current-user.schema';
import { apiFetch } from './fetch.api';

const CURRENT_USER_PATH = '/api/auth/me';
const UNAUTHORIZED_STATUS = 401;

export async function fetchCurrentUser(): Promise<CurrentUserResult> {
  const response = await apiFetch(CURRENT_USER_PATH, {
    method: 'GET',
    credentials: 'include',
  });

  if (response.status === UNAUTHORIZED_STATUS) {
    return { authenticated: false };
  }

  const user = currentUserResponseSchema.parse(await response.json());
  return { authenticated: true, user };
}
