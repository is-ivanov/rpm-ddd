import type { LoginRequest } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

export async function login(_request: LoginRequest): Promise<void> {
  void BASE_URL;
  throw new Error('Not implemented');
}
