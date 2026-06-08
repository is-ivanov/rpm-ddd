import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import { mapLoginErrorToView } from '../logic/login-error-view.logic';
import { LoginError } from '../logic/types';

describe('Login Error View Mapping', () => {
  it('maps an unexpected (non-LoginError) failure to the generic error view', async () => {
    await issue('127');

    const view = mapLoginErrorToView(new TypeError('fetch failed'));

    expect(view).toEqual({
      errorMessage: 'Something went wrong. Please try again.',
      requiresActivation: false,
      fieldErrors: {},
    });
  });

  it('passes a LoginError message and requiresActivation flag through to the view', async () => {
    await issue('127');

    const view = mapLoginErrorToView(new LoginError('Account not activated', true));

    expect(view).toEqual({
      errorMessage: 'Account not activated',
      requiresActivation: true,
      fieldErrors: {},
    });
  });

  it('maps per-field errors to their controls and clears the global banner on a 422', async () => {
    await issue('131');

    const view = mapLoginErrorToView(
      new LoginError('Validation failed', false, [
        { property: 'login', message: 'Username is required' },
        { property: 'password', message: 'Password is required' },
      ]),
    );

    expect(view).toEqual({
      errorMessage: '',
      requiresActivation: false,
      fieldErrors: { login: 'Username is required', password: 'Password is required' },
    });
  });

  it('ignores an unknown field-error property while still clearing the global banner', async () => {
    await issue('131');

    const view = mapLoginErrorToView(
      new LoginError('Validation failed', false, [{ property: 'captcha', message: 'Invalid captcha' }]),
    );

    expect(view).toEqual({
      errorMessage: '',
      requiresActivation: false,
      fieldErrors: {},
    });
  });
});
