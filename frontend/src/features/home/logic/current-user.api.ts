import type { CurrentUserResult } from './types';

export function fetchCurrentUser(): Promise<CurrentUserResult> {
  return Promise.reject(new Error('not implemented'));
}
