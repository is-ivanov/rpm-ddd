import { describe, expect, it } from 'vitest';
import { type ComplexityRule, evaluateComplexityRules } from '../logic/password-strength.logic';

function rule(key: string, label: string, met: boolean): ComplexityRule {
  return { key, label, met };
}

describe('Activation Password Complexity Rules', () => {
  // Story 1 §4.2: the activation form indicates password complexity PER RULE — each of the 6
  // rules is highlighted as met/unmet (E2E testid suffixes: length, uppercase, lowercase, digit,
  // special, no-spaces). evaluateComplexityRules returns the 6 rules in mockup/PASSWORD_RULES
  // order with stable key, human label, and a met flag for the given password.
  it.fails('marks only lowercase + no-spaces as met for a short lowercase value', () => {
    expect(evaluateComplexityRules('weak')).toEqual([
      rule('length', 'At least 12 characters', false),
      rule('uppercase', 'At least one uppercase letter', false),
      rule('lowercase', 'At least one lowercase letter', true),
      rule('digit', 'At least one digit', false),
      rule('special', 'At least one special character', false),
      rule('no-spaces', 'No spaces', true),
    ]);
  });

  it.fails('marks all six rules as met for a value satisfying every rule', () => {
    expect(evaluateComplexityRules('Str0ng-P@ssw0rd!')).toEqual([
      rule('length', 'At least 12 characters', true),
      rule('uppercase', 'At least one uppercase letter', true),
      rule('lowercase', 'At least one lowercase letter', true),
      rule('digit', 'At least one digit', true),
      rule('special', 'At least one special character', true),
      rule('no-spaces', 'No spaces', true),
    ]);
  });

  it.fails('marks only no-spaces as met for an empty value', () => {
    expect(evaluateComplexityRules('')).toEqual([
      rule('length', 'At least 12 characters', false),
      rule('uppercase', 'At least one uppercase letter', false),
      rule('lowercase', 'At least one lowercase letter', false),
      rule('digit', 'At least one digit', false),
      rule('special', 'At least one special character', false),
      rule('no-spaces', 'No spaces', true),
    ]);
  });
});
