export type PasswordStrength = 'weak' | 'medium' | 'strong';

export function computePasswordStrength(_password: string): PasswordStrength {
  throw new Error('not implemented');
}
