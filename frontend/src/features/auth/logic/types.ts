export interface LoginRequest {
  readonly login: string;
  readonly password: string;
}

export class LoginError extends Error {
  readonly requiresActivation: boolean;

  constructor(message: string | undefined, requiresActivation: boolean) {
    super(message);
    this.requiresActivation = requiresActivation;
  }
}
