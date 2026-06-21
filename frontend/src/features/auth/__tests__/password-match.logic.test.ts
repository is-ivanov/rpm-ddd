import { describe, expect, it } from 'vitest';
import { type PasswordMatch, evaluatePasswordMatch } from '../logic/password-match.logic';

// Exact text the activation form must show when the two fields differ — the single source of truth
// the component renders into data-testid="password-mismatch-error" (E2E contract, Story 1 §4.3).
const MISMATCH_MESSAGE = 'Passwords do not match';

const MATCHED: PasswordMatch = { matched: true, error: '' };
const MISMATCHED: PasswordMatch = { matched: false, error: MISMATCH_MESSAGE };

describe('Activation Password Match', () => {
  // Story 1 §4.3: the activation form shows an error when password !== confirm. The check is a pure
  // client-side comparison producing matched (no error) vs mismatched (the literal message). Cases
  // cover identical values, differing values, and the empty-field edge states.
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
