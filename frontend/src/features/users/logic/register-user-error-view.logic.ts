import { RegisterUserError } from './register-user.types';

export interface RegisterUserFieldErrors {
  readonly login?: string;
  readonly email?: string;
}

export function mapRegisterUserErrorToFieldErrors(error: unknown): RegisterUserFieldErrors {
  if (!(error instanceof RegisterUserError)) {
    return {};
  }

  const fieldErrors: { login?: string; email?: string } = {};
  for (const fieldError of error.fieldErrors) {
    if (fieldError.property === 'login') {
      fieldErrors.login = fieldError.message;
    } else if (fieldError.property === 'email') {
      fieldErrors.email = fieldError.message;
    }
  }
  return fieldErrors;
}
