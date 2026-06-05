import type { ActivationTokenResponse } from './types';

export async function validateActivationToken(token: string): Promise<ActivationTokenResponse> {
  throw new Error(`not implemented: validateActivationToken(${token})`);
}
