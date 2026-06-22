import type { CurrentUserResult } from './types';
import { currentUserResponseSchema } from '../schemas/current-user.schema';
import { apiUrl } from '@/app/logic/fetch.api';

const CURRENT_USER_PATH = '/api/auth/me';
const UNAUTHORIZED_STATUS = 401;

export async function fetchCurrentUser(): Promise<CurrentUserResult> {
  const response = await fetch(apiUrl(CURRENT_USER_PATH), {
    method: 'GET',
    credentials: 'include',
  });

  if (response.status === UNAUTHORIZED_STATUS) {
    return { authenticated: false };
  }

  const user = currentUserResponseSchema.parse(await response.json());
  return { authenticated: true, user };
}
