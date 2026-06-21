import { describe, expect, it } from 'vitest';
import { type ComplexityRule, evaluateComplexityRules } from '../logic/password-strength.logic';

// The 6 rules in the order evaluateComplexityRules must return them (mockup / PASSWORD_RULES order).
// key matches the E2E testid suffix (complexity-rule-{key}); label is the human text shown in the UI.
const RULES: ReadonlyArray<Omit<ComplexityRule, 'met'>> = [
  { key: 'length', label: 'At least 12 characters' },
  { key: 'uppercase', label: 'At least one uppercase letter' },
  { key: 'lowercase', label: 'At least one lowercase letter' },
  { key: 'digit', label: 'At least one digit' },
  { key: 'special', label: 'At least one special character' },
  { key: 'no-spaces', label: 'No spaces' },
];

// Builds the expected result by attaching hand-specified met flags to the fixed key/label contract.
// metFlags are literal test data (never derived from the password) — order matches RULES.
function expected(metFlags: readonly boolean[]): ComplexityRule[] {
  return RULES.map((rule, index) => ({ ...rule, met: metFlags[index] }));
}

describe('Activation Password Complexity Rules', () => {
  // Story 1 §4.2: the activation form indicates password complexity PER RULE — each of the 6 rules
  // is reported as met/unmet for the typed value. Cases cover both states of every predicate plus
  // the length 11/12 boundary; the [met=false for no-spaces] case (a value containing a space) is
  // the gap a single representative password would miss.
  it.each([
    { name: 'empty value → only no-spaces met', password: '', met: [false, false, false, false, false, true] },
    {
      name: 'short lowercase value → lowercase + no-spaces met',
      password: 'weak',
      met: [false, false, true, false, false, true],
    },
    {
      name: 'value satisfying every rule → all six met',
      password: 'Str0ng-P@ssw0rd!',
      met: [true, true, true, true, true, true],
    },
    {
      name: 'eleven-char strong value → length unmet at the boundary (11 < 12)',
      password: 'Ab1!Ab1!Abc',
      met: [false, true, true, true, true, true],
    },
    {
      name: 'twelve-char strong value → length met at the boundary (12 == 12)',
      password: 'Ab1!Ab1!Abcd',
      met: [true, true, true, true, true, true],
    },
    {
      name: 'otherwise-strong value containing a space → no-spaces unmet',
      password: 'Ab1! Ab1!Abc',
      met: [true, true, true, true, true, false],
    },
    {
      name: 'twelve uppercase letters → only length + uppercase + no-spaces met',
      password: 'ABCDEFGHIJKL',
      met: [true, true, false, false, false, true],
    },
  ])('$name', ({ password, met }) => {
    expect(evaluateComplexityRules(password)).toEqual(expected(met));
  });
});
