import type { LoginFieldError, LoginRequest, ProblemDetail, ProblemFieldError } from './types';
import { LoginError } from './types';
import { postJsonWithCsrf } from './csrf';

const AUTHENTICATION_FAILED_TYPE = 'https://www.rpm-ddd.my/problem/authentication-failed';

function toLoginFieldError(fieldError: ProblemFieldError): LoginFieldError {
  return { property: fieldError.property, message: fieldError.message };
}

function parseFieldErrors(problem: ProblemDetail): ReadonlyArray<LoginFieldError> {
  return (problem.fieldErrors ?? []).map(toLoginFieldError);
}

async function throwLoginError(response: Response): Promise<never> {
  const problem = (await response.json()) as ProblemDetail;
  throw new LoginError(problem.detail, problem.type === AUTHENTICATION_FAILED_TYPE, parseFieldErrors(problem));
}

export async function login(request: LoginRequest): Promise<void> {
  const response = await postJsonWithCsrf('/api/auth/login', request);

  if (!response.ok) {
    await throwLoginError(response);
  }
}
