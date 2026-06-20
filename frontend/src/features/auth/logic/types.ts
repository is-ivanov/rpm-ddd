import type { ProblemDetail, ProblemFieldError } from '@/app/schemas/problem-detail.schema';
import type { ActivationTokenResponse } from '../schemas/activation-token.schema';

export type { ProblemDetail, ProblemFieldError, ActivationTokenResponse };

export interface LoginRequest {
  readonly login: string;
  readonly password: string;
}

export class ActivationError extends Error {}

export class LoginError extends Error {
  readonly requiresActivation: boolean;
  readonly fieldErrors: ReadonlyArray<ProblemFieldError>;

  constructor(
    message: string | undefined,
    requiresActivation: boolean,
    fieldErrors: ReadonlyArray<ProblemFieldError> = [],
  ) {
    super(message);
    this.requiresActivation = requiresActivation;
    this.fieldErrors = fieldErrors;
  }
}
