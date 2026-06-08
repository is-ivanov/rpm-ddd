import { LoginError, type LoginFieldError } from './types';

export interface LoginFieldErrors {
  readonly login?: string;
  readonly password?: string;
}

export interface LoginErrorView {
  readonly errorMessage: string;
  readonly requiresActivation: boolean;
  readonly fieldErrors: LoginFieldErrors;
}

const FIELD_CONTROLS = new Set<string>(['login', 'password']);

function isFieldControl(property: string): property is keyof LoginFieldErrors {
  return FIELD_CONTROLS.has(property);
}

function toFieldErrorsMap(fieldErrors: ReadonlyArray<LoginFieldError>): LoginFieldErrors {
  return fieldErrors.reduce<LoginFieldErrors>(
    (map, { property, message }) => (isFieldControl(property) ? { ...map, [property]: message } : map),
    {},
  );
}

function viewFromLoginError(error: LoginError): LoginErrorView {
  const hasFieldErrors = error.fieldErrors.length > 0;
  return {
    errorMessage: hasFieldErrors ? '' : error.message,
    requiresActivation: error.requiresActivation,
    fieldErrors: toFieldErrorsMap(error.fieldErrors),
  };
}

export function mapLoginErrorToView(error: unknown): LoginErrorView {
  if (error instanceof LoginError) {
    return viewFromLoginError(error);
  }
  return {
    errorMessage: 'Something went wrong. Please try again.',
    requiresActivation: false,
    fieldErrors: {},
  };
}
