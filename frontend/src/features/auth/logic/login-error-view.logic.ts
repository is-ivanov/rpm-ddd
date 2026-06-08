import { LoginError } from './types';

export interface LoginFieldErrors {
  readonly login?: string;
  readonly password?: string;
}

export interface LoginErrorView {
  readonly errorMessage: string;
  readonly requiresActivation: boolean;
  readonly fieldErrors: LoginFieldErrors;
}

export function mapLoginErrorToView(error: unknown): LoginErrorView {
  if (error instanceof LoginError) {
    return { errorMessage: error.message, requiresActivation: error.requiresActivation, fieldErrors: {} };
  }
  return {
    errorMessage: 'Something went wrong. Please try again.',
    requiresActivation: false,
    fieldErrors: {},
  };
}
