import type { ActivationTokenResponse } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export async function validateActivationToken(token: string): Promise<ActivationTokenResponse> {
  const response = await fetch(`${BASE_URL}/api/auth/activate?token=${encodeURIComponent(token)}`, {
    method: 'GET',
    credentials: 'include',
  });

  return (await response.json()) as ActivationTokenResponse;
}
