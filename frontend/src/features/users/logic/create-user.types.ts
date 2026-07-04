import type { ProblemFieldError } from '@/app/schemas/problem-detail.schema';

export type { ProblemFieldError };

export interface CreateUserRequest {
  readonly firstName: string;
  readonly middleName: string | null;
  readonly lastName: string;
  readonly login: string;
  readonly email: string;
  readonly timeZone: string;
}

export class CreateUserError extends Error {
  readonly fieldErrors: ReadonlyArray<ProblemFieldError>;

  constructor(message: string | undefined, fieldErrors: ReadonlyArray<ProblemFieldError> = []) {
    super(message);
    this.fieldErrors = fieldErrors;
  }
}
