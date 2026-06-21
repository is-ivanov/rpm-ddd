import { describe, expect, it } from 'vitest';
import { computePasswordStrength } from '../logic/password-strength.logic';

describe('Activation Password Strength Classification', () => {
  // Story 1 §4.2: classification is based on how many of the 6 activation complexity rules
  // the value satisfies (>=12 chars, upper, lower, digit, special, no spaces) -> weak (<=3),
  // medium (4-5), strong (6). The E2E-pinned inputs ('weak' -> weak, 'Str0ng-P@ssw0rd!' ->
  // strong) and the band boundaries are asserted here.
  it('classifies a short lowercase-only password as weak (E2E-pinned input)', () => {
    expect(computePasswordStrength('weak')).toBe('weak');
  });

  it('classifies a value satisfying all six complexity rules as strong (E2E-pinned input)', () => {
    expect(computePasswordStrength('Str0ng-P@ssw0rd!')).toBe('strong');
  });

  it('treats a value satisfying three or fewer rules as weak', () => {
    expect(computePasswordStrength('abcABC')).toBe('weak');
  });

  it('treats a value satisfying four rules as medium (lower band boundary)', () => {
    expect(computePasswordStrength('abcdef1!')).toBe('medium');
  });

  it('treats a value satisfying five rules as medium (upper band boundary)', () => {
    expect(computePasswordStrength('Abcdefghijk1')).toBe('medium');
  });

  it('classifies an empty value as weak', () => {
    expect(computePasswordStrength('')).toBe('weak');
  });
});
