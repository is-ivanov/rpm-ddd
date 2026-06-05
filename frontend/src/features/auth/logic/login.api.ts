import type { LoginRequest } from './types';
import { LoginError } from './types';

const BASE_URL = import.meta.env.VITE_API_URL ?? '';

interface ProblemDetail {
  readonly type?: string;
  readonly detail?: string;
}

export async function login(request: LoginRequest): Promise<void> {
  const response = await fetch(`${BASE_URL}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify(request),
  });

  if (!response.ok) {
    const problem = (await response.json()) as ProblemDetail;
    throw new LoginError(problem.detail, false);
  }
}
