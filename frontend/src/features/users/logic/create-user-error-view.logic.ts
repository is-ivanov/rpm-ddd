import { CreateUserError } from './create-user.types';

export interface CreateUserFieldErrors {
  readonly login?: string;
  readonly email?: string;
}

export function mapCreateUserErrorToFieldErrors(error: unknown): CreateUserFieldErrors {
  void error;
  void CreateUserError;
  return {};
}
