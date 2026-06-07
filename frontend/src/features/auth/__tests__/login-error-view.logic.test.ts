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
    });
  });

  it('passes a LoginError message and requiresActivation flag through to the view', async () => {
    await issue('127');

    const view = mapLoginErrorToView(new LoginError('Account not activated', true));

    expect(view).toEqual({
      errorMessage: 'Account not activated',
      requiresActivation: true,
    });
  });
});
