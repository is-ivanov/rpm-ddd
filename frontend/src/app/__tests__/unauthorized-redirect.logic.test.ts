import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import { shouldRedirectToLogin } from '../logic/unauthorized-redirect.logic';

describe('Unauthorized Redirect Decision', () => {
  // RED — shouldRedirectToLogin not implemented (throws 'Not implemented')
  it.fails.each([
    {
      name: 'redirects to login when a response is 401 and the user is not on the login route',
      status: 401,
      path: '/activate',
      redirect: true,
    },
    { name: 'does not redirect on 403', status: 403, path: '/activate', redirect: false },
    { name: 'does not redirect on 401 when already on the login route', status: 401, path: '/login', redirect: false },
  ])('$name', async ({ status, path, redirect }) => {
    await issue('162');

    expect(shouldRedirectToLogin(status, path)).toBe(redirect);
  });
});
