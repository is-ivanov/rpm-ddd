export const LOGIN_PATH = '/login';

/** True when an API response indicates the session is no longer authenticated. */
export function isUnauthorized(status: number): boolean {
  return status === 401;
}

/** True when a guarded route is entered without an authenticated session. */
export function shouldRedirectToLogin(requiresAuth: boolean, isAuthenticated: boolean): boolean {
  return requiresAuth && !isAuthenticated;
}

/** True when an authenticated session is lost mid-page (authenticated -> unauthenticated). */
export function shouldRedirectOnSessionLoss(wasAuthenticated: boolean, isAuthenticated: boolean): boolean {
  return wasAuthenticated && !isAuthenticated;
}
