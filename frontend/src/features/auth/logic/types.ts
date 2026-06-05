export interface LoginRequest {
  readonly login: string;
  readonly password: string;
}

export class LoginError extends Error {}
