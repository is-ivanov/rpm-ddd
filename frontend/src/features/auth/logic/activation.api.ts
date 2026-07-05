import type { ActivationTokenResponse } from './types';
import { ActivationError } from './types';
import { postJsonWithCsrf } from './csrf';
import { apiFetch } from '@/app/logic/fetch.api';
import { problemDetailSchema } from '@/app/schemas/problem-detail.schema';
import { activationTokenResponseSchema } from '../schemas/activation-token.schema';

export async function validateActivationToken(token: string): Promise<ActivationTokenResponse> {
  const response = await apiFetch(`/api/auth/activate?token=${encodeURIComponent(token)}`, {
    method: 'GET',
    credentials: 'include',
  });

  await throwIfProblem(response);

  const body: unknown = await response.json();
  const parsed = activationTokenResponseSchema.safeParse(body);
  if (!parsed.success) {
    throw new ActivationError('Malformed activation response.');
  }
  return parsed.data;
}

export async function activateAccount(token: string, password: string): Promise<void> {
  const response = await postJsonWithCsrf('/api/auth/activate', { token, password });
  await throwIfProblem(response);
}

async function throwIfProblem(response: Response): Promise<void> {
  if (!response.ok) {
    const problem = problemDetailSchema.parse(await response.json());
    throw new ActivationError(problem.detail);
  }
}
