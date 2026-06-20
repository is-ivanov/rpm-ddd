import { GENERIC_SUBMIT_ERROR_MESSAGE } from './error-copy';
import { ActivationError } from './types';

export interface ActivationSubmitErrorView {
  readonly errorMessage: string;
}

export function mapActivationSubmitErrorToView(error: unknown): ActivationSubmitErrorView {
  if (error instanceof ActivationError) {
    return { errorMessage: error.message };
  }
  return { errorMessage: GENERIC_SUBMIT_ERROR_MESSAGE };
}
