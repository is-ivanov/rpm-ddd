export type { ProblemDetail, ProblemFieldError } from '@/app/schemas/problem-detail.schema';
export type { ActivationTokenResponse } from '../schemas/activation-token.schema';

export interface LoginRequest {
  readonly login: string;
  readonly password: string;
}

export class ActivationError extends Error {}

export interface LoginFieldError {
  readonly property: string;
  readonly message: string;
}

export class LoginError extends Error {
  readonly requiresActivation: boolean;
  readonly fieldErrors: ReadonlyArray<LoginFieldError>;

  constructor(
    message: string | undefined,
    requiresActivation: boolean,
    fieldErrors: ReadonlyArray<LoginFieldError> = [],
  ) {
    super(message);
    this.requiresActivation = requiresActivation;
    this.fieldErrors = fieldErrors;
  }
}
