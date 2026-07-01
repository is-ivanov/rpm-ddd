import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import {
  isUnauthorized,
  shouldRedirectOnSessionLoss,
  shouldRedirectToLogin,
} from '../logic/unauthorized-redirect.logic';

describe('Unauthorized Response Decision', () => {
  it.each([
    { name: 'flags a 401 response as unauthorized', status: 401, expected: true },
    { name: 'does not flag a 403 response as unauthorized', status: 403, expected: false },
    { name: 'does not flag a 200 response as unauthorized', status: 200, expected: false },
  ])('$name', ({ status, expected }) => {
    expect(isUnauthorized(status)).toBe(expected);
  });
});

describe('Protected Route Redirect Decision', () => {
  it.each([
    {
      name: 'redirects when a protected route is entered without a session',
      requiresAuth: true,
      authed: false,
      redirect: true,
    },
    { name: 'allows a protected route for an authenticated user', requiresAuth: true, authed: true, redirect: false },
    { name: 'allows a public route for a guest', requiresAuth: false, authed: false, redirect: false },
    { name: 'allows a public route for an authenticated user', requiresAuth: false, authed: true, redirect: false },
  ])('$name', ({ requiresAuth, authed, redirect }) => {
    expect(shouldRedirectToLogin(requiresAuth, authed)).toBe(redirect);
  });
});

describe('Session Loss Redirect Decision', () => {
  // RED (#251) — shouldRedirectOnSessionLoss not implemented yet: only an
  // authenticated -> unauthenticated transition must redirect.
  it.fails('redirects only on an authenticated -> unauthenticated transition', async () => {
    await issue('251');
    expect(shouldRedirectOnSessionLoss(true, false)).toBe(true);
    expect(shouldRedirectOnSessionLoss(true, true)).toBe(false);
    expect(shouldRedirectOnSessionLoss(false, false)).toBe(false);
    expect(shouldRedirectOnSessionLoss(false, true)).toBe(false);
  });
});
