import { describe, expect, it } from 'vitest';
import { issue } from 'allure-js-commons';
import { mapActivationSubmitErrorToView } from '../logic/activation-error-view.logic';
import { ActivationError } from '../logic/types';

describe('Activation Submit Error View Mapping', () => {
  // RED — mapActivationSubmitErrorToView is a stub (throws 'Not implemented') until GREEN
  it.fails('passes an ActivationError message through to the error view', async () => {
    await issue('188');

    const view = mapActivationSubmitErrorToView(
      new ActivationError('Password does not meet the complexity requirements.'),
    );

    expect(view).toEqual({
      errorMessage: 'Password does not meet the complexity requirements.',
    });
  });

  // RED — mapActivationSubmitErrorToView is a stub (throws 'Not implemented') until GREEN
  it.fails('maps an unexpected (non-ActivationError) failure to the generic error view', async () => {
    await issue('188');

    const view = mapActivationSubmitErrorToView(new TypeError('fetch failed'));

    expect(view).toEqual({
      errorMessage: 'Something went wrong. Please try again.',
    });
  });
});
