export const LOGIN_PATH = '/login';

export function shouldRedirectToLogin(status: number, currentPath: string): boolean {
  return status === 401 && currentPath !== LOGIN_PATH;
}
