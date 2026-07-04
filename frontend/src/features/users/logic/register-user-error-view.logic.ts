import { RegisterUserError } from './register-user.types';

export interface RegisterUserFieldErrors {
  readonly login?: string;
  readonly email?: string;
}

export function mapRegisterUserErrorToFieldErrors(error: unknown): RegisterUserFieldErrors {
  void error;
  void RegisterUserError;
  return {};
}
