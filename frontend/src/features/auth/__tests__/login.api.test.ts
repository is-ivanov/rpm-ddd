import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { login } from '../logic/login.api';
import { LoginError } from '../logic/types';
import type { LoginRequest } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

function stubLoginProblem(problem: { type: string; detail: string }): void {
  server.use(
    http.post(`${BASE}/api/auth/login`, () =>
      HttpResponse.json(
        {
          type: problem.type,
          title: 'Unauthorized',
          status: 401,
          detail: problem.detail,
          instance: '/api/auth/login',
        },
        { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
      ),
    ),
  );
}

function captureLoginRejection(request: LoginRequest): Promise<unknown> {
  return login(request).then(
    () => {
      throw new Error('login resolved but should have rejected on 401');
    },
    (rejected: unknown) => rejected,
  );
}

describe('Login API Client', () => {
  it('surfaces the problem+json detail as a LoginError on 401 invalid credentials', async () => {
    stubLoginProblem({
      type: 'https://www.rpm-ddd.my/problem/bad-credentials',
      detail: 'Bad credentials',
    });

    const error = await captureLoginRejection({ login: 'ivan', password: 'wrong-pass' });

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).message).toBe('Bad credentials');
    expect((error as LoginError).requiresActivation).toBe(false);
  });

  it('flags requiresActivation on 401 account not activated', async () => {
    stubLoginProblem({
      type: 'https://www.rpm-ddd.my/problem/authentication-failed',
      detail: 'Account not activated',
    });

    const error = await captureLoginRejection({ login: 'ivan', password: 'right-pass' });

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).message).toBe('Account not activated');
    expect((error as LoginError).requiresActivation).toBe(true);
  });
});
