import { LoginError } from './types';

export interface LoginErrorView {
  readonly errorMessage: string;
  readonly requiresActivation: boolean;
}

export function mapLoginErrorToView(error: unknown): LoginErrorView {
  // TDD Red Phase - not implemented (issue #127); green-frontend will branch on
  // LoginError vs unexpected errors. void-reference both symbols to keep them
  // wired for green without satisfying no-unused-vars by deleting them.
  void error;
  void LoginError;
  throw new Error('Not implemented');
}
