import { describe, expect, it } from 'vitest';
import { http, HttpResponse } from 'msw';
import { server } from '@/test/msw-server';
import { login } from '../logic/login.api';
import { LoginError } from '../logic/types';

const BASE = import.meta.env.VITE_API_URL;

describe('Login API Client', () => {
  it('surfaces the problem+json detail as a LoginError on 401 invalid credentials', async () => {
    server.use(
      http.post(`${BASE}/api/auth/login`, () =>
        HttpResponse.json(
          {
            type: 'about:blank',
            title: 'Unauthorized',
            status: 401,
            detail: 'Invalid username or password',
            instance: '/api/auth/login',
          },
          { status: 401, headers: { 'Content-Type': 'application/problem+json' } },
        ),
      ),
    );

    const error = await login({ login: 'ivan', password: 'wrong-pass' }).then(
      () => {
        throw new Error('login resolved but should have rejected on 401');
      },
      (rejected: unknown) => rejected,
    );

    expect(error).toBeInstanceOf(LoginError);
    expect((error as LoginError).message).toBe('Invalid username or password');
  });
});
