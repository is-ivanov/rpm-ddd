import { describe, expect, it } from 'vitest';
import { evaluatePasswordMatch, type PasswordMatch } from '../logic/password-match.logic';

const MISMATCH_MESSAGE = 'Passwords do not match';

const MATCHED: PasswordMatch = { matched: true, error: '' };
const MISMATCHED: PasswordMatch = { matched: false, error: MISMATCH_MESSAGE };

describe('Activation Password Match', () => {
  it.each([
    {
      name: 'identical values → matched, no error',
      password: 'Str0ng-P@ssw0rd!',
      confirm: 'Str0ng-P@ssw0rd!',
      expected: MATCHED,
    },
    {
      name: 'different values → mismatch error',
      password: 'Str0ng-P@ssw0rd!',
      confirm: 'Different-P@ssw0rd!',
      expected: MISMATCHED,
    },
    { name: 'both empty → matched, no error', password: '', confirm: '', expected: MATCHED },
    { name: 'only confirm empty → mismatch error', password: 'Str0ng-P@ssw0rd!', confirm: '', expected: MISMATCHED },
    { name: 'only password empty → mismatch error', password: '', confirm: 'Str0ng-P@ssw0rd!', expected: MISMATCHED },
  ])('$name', ({ password, confirm, expected }) => {
    expect(evaluatePasswordMatch(password, confirm)).toEqual(expected);
  });
});
