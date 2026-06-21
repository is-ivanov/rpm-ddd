import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import { computePasswordStrength } from '../logic/password-strength.logic';

describe('Activation Password Strength Classification', () => {
  // RED (Story 1 §4.2): computePasswordStrength is a not-implemented stub that throws.
  // it.fails() keeps this test green while the body fails and turns the build red once
  // the function is implemented. The pinned assertions below define the strength contract:
  // classification is based on how many of the 6 activation complexity rules the value
  // satisfies (>=12 chars, upper, lower, digit, special, no spaces) -> weak (<=3),
  // medium (4-5), strong (6). The E2E-pinned inputs ('weak' -> weak, 'Str0ng-P@ssw0rd!' ->
  // strong) and the band boundaries are asserted here.
  it.fails('classifies a short lowercase-only password as weak (E2E-pinned input)', async () => {
    await issue('189');

    expect(computePasswordStrength('weak')).toBe('weak');
  });

  it.fails('classifies a value satisfying all six complexity rules as strong (E2E-pinned input)', async () => {
    await issue('189');

    expect(computePasswordStrength('Str0ng-P@ssw0rd!')).toBe('strong');
  });

  it.fails('treats a value satisfying three or fewer rules as weak', async () => {
    await issue('189');

    expect(computePasswordStrength('abcABC')).toBe('weak');
  });

  it.fails('treats a value satisfying four rules as medium (lower band boundary)', async () => {
    await issue('189');

    expect(computePasswordStrength('abcdef1!')).toBe('medium');
  });

  it.fails('treats a value satisfying five rules as medium (upper band boundary)', async () => {
    await issue('189');

    expect(computePasswordStrength('Abcdefghijk1')).toBe('medium');
  });

  it.fails('classifies an empty value as weak', async () => {
    await issue('189');

    expect(computePasswordStrength('')).toBe('weak');
  });
});
