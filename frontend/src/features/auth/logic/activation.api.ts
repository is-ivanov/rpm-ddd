import type { ActivationTokenResponse, ProblemDetail } from './types';
import { ActivationError } from './types';
import { primeCsrfToken } from './csrf';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export async function validateActivationToken(token: string): Promise<ActivationTokenResponse> {
  const response = await fetch(`${BASE_URL}/api/auth/activate?token=${encodeURIComponent(token)}`, {
    method: 'GET',
    credentials: 'include',
  });

  if (!response.ok) {
    const problem = (await response.json()) as ProblemDetail;
    throw new ActivationError(problem.detail);
  }

  return (await response.json()) as ActivationTokenResponse;
}

export async function activateAccount(token: string, password: string): Promise<void> {
  const xsrfToken = await primeCsrfToken();

  await fetch(`${BASE_URL}/api/auth/activate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': xsrfToken,
    },
    body: JSON.stringify({ token, password }),
  });
}
