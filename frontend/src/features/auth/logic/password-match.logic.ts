export interface PasswordMatch {
  readonly matched: boolean;
  readonly error: string;
}

export function evaluatePasswordMatch(password: string, confirmPassword: string): PasswordMatch {
  if (password === confirmPassword) {
    return { matched: true, error: '' };
  }
  return { matched: false, error: 'Passwords do not match' };
}
