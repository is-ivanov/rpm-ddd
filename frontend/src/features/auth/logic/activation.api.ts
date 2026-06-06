import type { ActivationTokenResponse, ProblemDetail } from './types';
import { ActivationError } from './types';

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

function readCookie(name: string): string {
  const match = new RegExp(`(?:^|; )${name}=([^;]*)`).exec(document.cookie);
  return match ? decodeURIComponent(match[1]) : '';
}

export async function activateAccount(token: string, password: string): Promise<void> {
  await fetch(`${BASE_URL}/api/auth/csrf`, {
    method: 'GET',
    credentials: 'include',
  });

  await fetch(`${BASE_URL}/api/auth/activate`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      'X-XSRF-TOKEN': readCookie('XSRF-TOKEN'),
    },
    body: JSON.stringify({ token, password }),
  });
}
