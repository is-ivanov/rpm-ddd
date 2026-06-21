import {describe, expect, it} from 'vitest';
import {type ComplexityRule, evaluateComplexityRules} from '../logic/password-strength.logic';

const RULES: ReadonlyArray<Omit<ComplexityRule, 'met'>> = [
  { key: 'length', label: 'At least 12 characters' },
  { key: 'uppercase', label: 'At least one uppercase letter' },
  { key: 'lowercase', label: 'At least one lowercase letter' },
  { key: 'digit', label: 'At least one digit' },
  { key: 'special', label: 'At least one special character' },
  { key: 'no-spaces', label: 'No spaces' },
];

function expected(metFlags: readonly boolean[]): ComplexityRule[] {
  return RULES.map((rule, index) => ({ ...rule, met: metFlags[index] }));
}

describe('Activation Password Complexity Rules', () => {
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
