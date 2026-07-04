import { describe, expect, it } from 'vitest';
import { mapCreateUserErrorToFieldErrors } from '../logic/create-user-error-view.logic';
import { CreateUserError } from '../logic/create-user.types';

describe('Create User Error View Mapping', () => {
  it('maps an unexpected (non-CreateUserError) failure to an empty field-error map', () => {
    const fieldErrors = mapCreateUserErrorToFieldErrors(new TypeError('network down'));

    expect(fieldErrors).toEqual({});
  });

  // RED — mapCreateUserErrorToFieldErrors not implemented (stub returns {})
  it.fails('maps a duplicate-login error to the login control', () => {
    const fieldErrors = mapCreateUserErrorToFieldErrors(
      new CreateUserError('Validation failed', [{ property: 'login', message: 'Login already exists' }]),
    );

    expect(fieldErrors).toEqual({ login: 'Login already exists' });
  });

  // RED — mapCreateUserErrorToFieldErrors not implemented (stub returns {})
  it.fails('maps a duplicate-email error to the email control', () => {
    const fieldErrors = mapCreateUserErrorToFieldErrors(
      new CreateUserError('Validation failed', [{ property: 'email', message: 'Email already exists' }]),
    );

    expect(fieldErrors).toEqual({ email: 'Email already exists' });
  });

  it('ignores an unknown field-error property', () => {
    const fieldErrors = mapCreateUserErrorToFieldErrors(
      new CreateUserError('Validation failed', [{ property: 'firstName', message: 'First name is required' }]),
    );

    expect(fieldErrors).toEqual({});
  });
});
