import type { CurrentUserResult } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';
const CURRENT_USER_PATH = '/api/auth/me';
const UNAUTHORIZED_STATUS = 401;

export async function fetchCurrentUser(): Promise<CurrentUserResult> {
  const response = await fetch(`${BASE_URL}${CURRENT_USER_PATH}`, {
    method: 'GET',
    credentials: 'include',
  });

  if (response.status === UNAUTHORIZED_STATUS) {
    return { authenticated: false };
  }

  throw new Error('Authenticated user mapping not implemented yet.');
}
