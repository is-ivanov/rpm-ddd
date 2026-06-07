import { LoginError } from './types';

export interface LoginErrorView {
  readonly errorMessage: string;
  readonly requiresActivation: boolean;
}

export function mapLoginErrorToView(error: unknown): LoginErrorView {
  if (error instanceof LoginError) {
    return { errorMessage: error.message, requiresActivation: error.requiresActivation };
  }
  return { errorMessage: 'Something went wrong. Please try again.', requiresActivation: false };
}
