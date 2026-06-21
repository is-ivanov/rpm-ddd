export interface PasswordMatch {
  readonly matched: boolean;
  readonly error: string;
}

export function evaluatePasswordMatch(_password: string, _confirmPassword: string): PasswordMatch {
  throw new Error('not implemented');
}
