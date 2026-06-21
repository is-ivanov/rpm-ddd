export type PasswordStrength = 'weak' | 'medium' | 'strong';

const COMPLEXITY_RULES: ReadonlyArray<(password: string) => boolean> = [
  (password) => password.length >= 12,
  (password) => /[A-Z]/.test(password),
  (password) => /[a-z]/.test(password),
  (password) => /\d/.test(password),
  (password) => /[^A-Za-z0-9\s]/.test(password),
  (password) => !/\s/.test(password),
];

const STRONG_THRESHOLD = COMPLEXITY_RULES.length;
const MEDIUM_THRESHOLD = 4;

export function computePasswordStrength(password: string): PasswordStrength {
  const score = COMPLEXITY_RULES.filter((rule) => rule(password)).length;
  if (score >= STRONG_THRESHOLD) {
    return 'strong';
  }
  if (score >= MEDIUM_THRESHOLD) {
    return 'medium';
  }
  return 'weak';
}
