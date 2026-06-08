import type { LoginRequest, ProblemDetail } from './types';
import { LoginError } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

const AUTHENTICATION_FAILED_TYPE = 'https://www.rpm-ddd.my/problem/authentication-failed';

function readCookie(name: string): string {
  const match = new RegExp(`(?:^|; )${name}=([^;]*)`).exec(document.cookie);
  return match ? decodeURIComponent(match[1]) : '';
}

async function throwLoginError(response: Response): Promise<never> {
  const problem = (await response.json()) as ProblemDetail;
  throw new LoginError(problem.detail, problem.type === AUTHENTICATION_FAILED_TYPE);
}

export async function login(request: LoginRequest): Promise<void> {
  await fetch(`${BASE_URL}/api/auth/csrf`, {
    method: 'GET',
    credentials: 'include',
  });

  const response = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': readCookie('XSRF-TOKEN'),
    },
    credentials: 'include',
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    await throwLoginError(response);
  }
}
