export interface ComplexityRule {
  readonly key: string;
  readonly label: string;
  readonly met: boolean;
}

interface ComplexityRuleSpec {
  readonly key: string;
  readonly label: string;
  readonly predicate: (password: string) => boolean;
}

const COMPLEXITY_RULES: ReadonlyArray<ComplexityRuleSpec> = [
  { key: 'length', label: 'At least 12 characters', predicate: (password) => password.length >= 12 },
  { key: 'uppercase', label: 'At least one uppercase letter', predicate: (password) => /[A-Z]/.test(password) },
  { key: 'lowercase', label: 'At least one lowercase letter', predicate: (password) => /[a-z]/.test(password) },
  { key: 'digit', label: 'At least one digit', predicate: (password) => /\d/.test(password) },
  {
    key: 'special',
    label: 'At least one special character',
    predicate: (password) => /[^A-Za-z0-9\s]/.test(password),
  },
  { key: 'no-spaces', label: 'No spaces', predicate: (password) => !/\s/.test(password) },
];

export function evaluateComplexityRules(password: string): ComplexityRule[] {
  return COMPLEXITY_RULES.map(({ key, label, predicate }) => ({ key, label, met: predicate(password) }));
}
