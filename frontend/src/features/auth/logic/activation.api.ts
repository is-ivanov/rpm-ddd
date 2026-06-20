import type { ActivationTokenResponse, ProblemDetail } from './types';
import { ActivationError } from './types';
import { postJsonWithCsrf } from './csrf';
import { apiFetch } from '@/app/logic/fetch.api';

async function throwIfProblem(response: Response): Promise<void> {
  if (!response.ok) {
    const problem = (await response.json()) as ProblemDetail;
    throw new ActivationError(problem.detail);
  }
}

export async function validateActivationToken(token: string): Promise<ActivationTokenResponse> {
  const response = await apiFetch(`/api/auth/activate?token=${encodeURIComponent(token)}`, {
    method: 'GET',
    credentials: 'include',
  });

  await throwIfProblem(response);

  return (await response.json()) as ActivationTokenResponse;
}

export async function activateAccount(token: string, password: string): Promise<void> {
  const response = await postJsonWithCsrf('/api/auth/activate', { token, password });
  await throwIfProblem(response);
}
