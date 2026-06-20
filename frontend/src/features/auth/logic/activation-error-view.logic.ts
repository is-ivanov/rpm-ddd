import { ActivationError } from './types';

export interface ActivationSubmitErrorView {
  readonly errorMessage: string;
}

export function mapActivationSubmitErrorToView(error: unknown): ActivationSubmitErrorView {
  void error;
  void ActivationError;
  throw new Error('Not implemented');
}
