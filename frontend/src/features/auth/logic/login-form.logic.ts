export function isLoginFormValid(login: string, password: string): boolean {
  return login.trim().length > 0 && password.trim().length > 0;
}
