export interface ProblemFieldError {
  readonly property: string;
  readonly message: string;
}

export interface ProblemDetail {
  readonly type?: string;
  readonly detail?: string;
  readonly fieldErrors?: ReadonlyArray<ProblemFieldError>;
}

export interface LoginRequest {
  readonly login: string;
  readonly password: string;
}

export interface ActivationTokenResponse {
  readonly login: string;
  readonly email: string;
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
