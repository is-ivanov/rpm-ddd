import type { LoginRequest, ProblemDetail } from './types';
import { LoginError } from './types';
import { primeCsrfToken } from './csrf';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

const AUTHENTICATION_FAILED_TYPE = 'https://www.rpm-ddd.my/problem/authentication-failed';

async function throwLoginError(response: Response): Promise<never> {
  const problem = (await response.json()) as ProblemDetail;
  throw new LoginError(problem.detail, problem.type === AUTHENTICATION_FAILED_TYPE);
}

export async function login(request: LoginRequest): Promise<void> {
  const xsrfToken = await primeCsrfToken();

  const response = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': xsrfToken,
    },
    credentials: 'include',
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await throwLoginError(response);
  }
}
