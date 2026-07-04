import { describe, expect, it } from 'vitest';
import { mapRegisterUserErrorToFieldErrors } from '../logic/register-user-error-view.logic';
import { RegisterUserError } from '../logic/register-user.types';

describe('Register User Error View Mapping', () => {
  it('maps an unexpected (non-RegisterUserError) failure to an empty field-error map', () => {
    const fieldErrors = mapRegisterUserErrorToFieldErrors(new TypeError('network down'));

    expect(fieldErrors).toEqual({});
  });

  // RED — mapRegisterUserErrorToFieldErrors not implemented (stub returns {})
  it.fails('maps a duplicate-login error to the login control', () => {
    const fieldErrors = mapRegisterUserErrorToFieldErrors(
      new RegisterUserError('Validation failed', [{ property: 'login', message: 'Login already exists' }]),
    );

    expect(fieldErrors).toEqual({ login: 'Login already exists' });
  });

  // RED — mapRegisterUserErrorToFieldErrors not implemented (stub returns {})
  it.fails('maps a duplicate-email error to the email control', () => {
    const fieldErrors = mapRegisterUserErrorToFieldErrors(
      new RegisterUserError('Validation failed', [{ property: 'email', message: 'Email already exists' }]),
    );

    expect(fieldErrors).toEqual({ email: 'Email already exists' });
  });

  it('ignores an unknown field-error property', () => {
    const fieldErrors = mapRegisterUserErrorToFieldErrors(
      new RegisterUserError('Validation failed', [{ property: 'firstName', message: 'First name is required' }]),
    );

    expect(fieldErrors).toEqual({});
  });
});
