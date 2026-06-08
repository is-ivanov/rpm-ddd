import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import { isLoginFormValid } from '../logic/login-form.logic';

describe('Login Form Validation', () => {
  it('returns false when both login and password are empty', async () => {
    await issue('131');

    expect(isLoginFormValid('', '')).toBe(false);
  });

  it('returns false when only login is filled', async () => {
    await issue('131');

    expect(isLoginFormValid('user@example.com', '')).toBe(false);
  });

  it('returns false when only password is filled', async () => {
    await issue('131');

    expect(isLoginFormValid('', 'secret')).toBe(false);
  });

  it('treats a whitespace-only value as empty', async () => {
    await issue('131');

    expect(isLoginFormValid('   ', 'secret')).toBe(false);
  });

  it('returns true when both login and password are non-empty', async () => {
    await issue('131');

    expect(isLoginFormValid('user@example.com', 'secret')).toBe(true);
  });
});
