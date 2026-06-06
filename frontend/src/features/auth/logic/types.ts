export interface ProblemDetail {
  readonly type?: string;
  readonly detail?: string;
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

export class LoginError extends Error {
  readonly requiresActivation: boolean;

  constructor(message: string | undefined, requiresActivation: boolean) {
    super(message);
    this.requiresActivation = requiresActivation;
  }
}
